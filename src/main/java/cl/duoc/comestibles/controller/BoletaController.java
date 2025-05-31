package cl.duoc.comestibles.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.duoc.comestibles.services.BoletaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/boletas")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;

    @PostMapping
    public ResponseEntity<String> crearBoleta(@RequestBody List<String> comidaIds) {
        String boletaId = boletaService.generarYSubirBoleta(comidaIds);
        return ResponseEntity.ok("Boleta creada con ID: " + boletaId);
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
    public ResponseEntity<List<String>> listar() {
        return ResponseEntity.ok(boletaService.listarBoletas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        boletaService.eliminarBoleta(id);
        return ResponseEntity.ok("Boleta eliminada");
    }
}
