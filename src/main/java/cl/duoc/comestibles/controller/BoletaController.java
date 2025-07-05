package cl.duoc.comestibles.controller;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.duoc.comestibles.dto.BoletaDto;
import cl.duoc.comestibles.dto.ComidaDto;
import cl.duoc.comestibles.model.Boleta;
import cl.duoc.comestibles.model.Comida;
import cl.duoc.comestibles.services.BoletaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/boletas")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;    
    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<BoletaDto> crearBoleta(@RequestBody List<String> comidaIds) {
        Boleta boleta = boletaService.generarYSubirBoleta(comidaIds);
        return ResponseEntity.ok(convertToDto(boleta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable String id) {
        byte[] contenido = boletaService.descargarBoleta(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(contenido);
    }

    @GetMapping
    public ResponseEntity<List<BoletaDto>> listar() {
        List<Boleta> boletas = boletaService.listarBoletas();
        List<BoletaDto> dtos = boletas.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        boletaService.eliminarBoleta(id);
        return ResponseEntity.ok("Boleta eliminada");
    }



    // conversiones dto

    private BoletaDto convertToDto(Boleta boleta) {
        List<ComidaDto> comidaDtos = boleta.getComidas().stream()
                .map(c -> new ComidaDto(c.getId(), c.getNombre(), c.getPrecio()))
                .collect(Collectors.toList());
        return new BoletaDto(boleta.getId(), comidaDtos, boleta.getTotal());
    }

    private Boleta convertToEntity(BoletaDto dto) {
        List<Comida> comidas = dto.getComidas().stream()
                .map(c -> new Comida(c.getId(), c.getNombre(), c.getPrecio()))
                .collect(Collectors.toList());

        String id = dto.getId() != null ? dto.getId() : UUID.randomUUID().toString();

        return new Boleta(id, comidas, dto.getTotal());
    }




    // rabbit

    @GetMapping("/leer")
    public Boleta leerSiguienteMensaje() {
        Boleta boleta = (Boleta) rabbitTemplate.receiveAndConvert("myQueue");
        if (boleta == null) {
            throw new RuntimeException("No hay mensajes pendientes en la cola");
        }
        return boleta;
    }

}
