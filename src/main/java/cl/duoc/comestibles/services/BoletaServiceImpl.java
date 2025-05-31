package cl.duoc.comestibles.services;

import cl.duoc.comestibles.dto.S3ObjectDto;
import cl.duoc.comestibles.model.Boleta;
import cl.duoc.comestibles.model.Comida;
import cl.duoc.comestibles.repository.ComidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoletaServiceImpl implements BoletaService {

    private final AwsS3Service s3Service;
    private final ComidaRepository comidaRepository;

    private final String bucket = "tiendacomestibles";

    @Override
    public String generarYSubirBoleta(List<String> comidaIds) {
        List<Comida> comidas = comidaIds.stream()
            .map(comidaRepository::findComidaByID)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Boleta boleta = new Boleta(comidas);

        StringBuilder contenido = new StringBuilder("Boleta ID: " + boleta.getId() + "\n");
        for (Comida c : comidas) {
            contenido.append("- ").append(c.getNombre()).append(": $").append(c.getPrecio()).append("\n");
        }
        contenido.append("Total: $").append(boleta.getTotal());

        List<String> keys = s3Service.listObjects(bucket).stream()
            .map(S3ObjectDto::getKey)
            .collect(Collectors.toList());

        // Obtener el numero maximo de carpeta actual
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

        String key = nuevaCarpeta + "/" + boleta.getId() + ".txt";
        s3Service.upload(bucket,
            key,
            contenido.toString().getBytes(StandardCharsets.UTF_8),
            "text/plain"
        );

        return boleta.getId();
    }

    @Override
    public byte[] descargarBoleta(String boletaId) {
        return s3Service.downloadAsBytes(bucket, boletaId + ".txt");
    }

    @Override
    public List<String> listarBoletas() {
        return s3Service.listObjects(bucket).stream()
                .map(obj -> obj.getKey())
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarBoleta(String boletaId) {
        s3Service.deleteObject(bucket, boletaId + ".txt");
    }
}
