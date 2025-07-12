package cl.duoc.comestibles.services;

import cl.duoc.comestibles.config.RabbitMQConfig;
import cl.duoc.comestibles.dto.S3ObjectDto;
import cl.duoc.comestibles.model.Boleta;
import cl.duoc.comestibles.model.Comida;
import cl.duoc.comestibles.repository.ComidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoletaServiceImpl implements BoletaService {

    private final AwsS3Service s3Service;
    private final ComidaRepository comidaRepository;
    private final RabbitTemplate rabbitTemplate;


    private final String bucket = "comestiblesbucket";

    @Override
    public Boleta generarYSubirBoleta(List<String> comidaIds) {
        List<Comida> comidas = comidaIds.stream()
            .map(comidaRepository::findComidaByID)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    
        Boleta boleta = new Boleta(comidas);
    
        List<String> keys = s3Service.listObjects(bucket).stream()
            .map(S3ObjectDto::getKey)
            .collect(Collectors.toList());
    
        int maxFolder = keys.stream()
            .map(key -> {
                String[] parts = key.split("/");
                try {
                    return Integer.parseInt(parts[0]);
                } catch (Exception e) {
                    return 0;
                }
            })
            .max(Integer::compareTo)
            .orElse(0);
    
        int nuevaCarpeta = maxFolder + 1;
    
        try {
            File tempFile = File.createTempFile("boleta-" + boleta.getId(), ".pdf");
    
            var writer = new com.itextpdf.kernel.pdf.PdfWriter(new FileOutputStream(tempFile));
            var pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            var document = new com.itextpdf.layout.Document(pdf);
    
            StringBuilder contenidoBoleta = new StringBuilder();
            contenidoBoleta.append("BOLETA ID: ").append(boleta.getId()).append("\n\n");
            contenidoBoleta.append("DETALLE:\n-----------------------------------\n");
    
            for (Comida c : comidas) {
                contenidoBoleta.append("- ").append(c.getNombre()).append(": $").append(c.getPrecio()).append("\n");
            }
    
            contenidoBoleta.append("-----------------------------------\n");
            contenidoBoleta.append("TOTAL: $").append(boleta.getTotal()).append("\n\n");
            contenidoBoleta.append("Gracias por su compra.");
    
            // Generar PDF con el mismo contenido
            document.add(new com.itextpdf.layout.element.Paragraph(contenidoBoleta.toString()));
            document.close();
    
            // Subir PDF a S3
            String key = nuevaCarpeta + "/" + boleta.getId() + ".pdf";
            s3Service.upload(bucket, key, tempFile);
    
            // Enviar contenido a la cola
            rabbitTemplate.convertAndSend(RabbitMQConfig.MAIN_EXCHANGE, "", contenidoBoleta.toString());
    
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de la boleta", e);
        }
    
        return boleta;
    }
    
    

    @Override
    public byte[] descargarBoleta(String boletaId) {
        return s3Service.downloadAsBytes(bucket, boletaId + ".pdf");
    }
    
    

    @Override
    public List<Boleta> listarBoletas() {
        return s3Service.listObjects(bucket).stream()
            .map(obj -> obj.getKey())
            .filter(key -> key.endsWith(".pdf"))
            .map(key -> {
                byte[] contenido = s3Service.downloadAsBytes(bucket, key);
                String texto = new String(contenido, StandardCharsets.UTF_8);
                return parseBoletaDesdeTexto(texto);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    @Override
    public void eliminarBoleta(String boletaId) {
        s3Service.deleteObject(bucket, boletaId + ".pdf");
    }



    private Boleta parseBoletaDesdeTexto(String texto) {
        try {
            String[] lineas = texto.split("\n");
            String id = lineas[0].replace("Boleta ID: ", "").trim();
    
            List<Comida> comidas = new java.util.ArrayList<>();
            int total = 0;
    
            for (int i = 1; i < lineas.length - 1; i++) {
                String linea = lineas[i].trim().substring(2);
                String[] partes = linea.split(": \\$");
                String nombre = partes[0];
                int precio = Integer.parseInt(partes[1]);
                comidas.add(new Comida(null, nombre, precio));
            }
    
            total = Integer.parseInt(lineas[lineas.length - 1].replace("Total: $", "").trim());
    
            return new Boleta(id, comidas, total);
        } catch (Exception e) {
            return null;
        }
    }
    
}
