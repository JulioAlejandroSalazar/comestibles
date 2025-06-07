package cl.duoc.comestibles.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.duoc.comestibles.dto.ComidaDto;
import cl.duoc.comestibles.model.Comida;
import cl.duoc.comestibles.services.ComidaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comida")
@RequiredArgsConstructor
public class ComidaController {

    private final ComidaService comidaService;

    @GetMapping
    public ResponseEntity<List<ComidaDto>> getAllComida() {
        List<Comida> comidas = comidaService.getAllComidas();
        if (comidas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ComidaDto> dtos = comidas.stream()
            .map(this::toDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComidaDto> getComidaById(@PathVariable String id) {
        Comida comida = comidaService.getComidaById(id);
        if (comida == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(comida));
    }

    @PostMapping
    public ResponseEntity<Void> createComidaEntity(@RequestBody ComidaDto dto) {
        comidaService.createComida(toEntity(dto));
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateComida(@PathVariable String id, @RequestBody ComidaDto dto) {
        if (!id.equals(dto.getId()) || comidaService.getComidaById(id) == null) {
            return ResponseEntity.badRequest().build();
        }
        comidaService.updateComida(id, toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComida(@PathVariable String id) {
        if (comidaService.getComidaById(id) == null) {
            return ResponseEntity.badRequest().build();
        }
        comidaService.deleteComida(id);
        return ResponseEntity.noContent().build();
    }

    

    // conversiones dto

    private ComidaDto toDto(Comida comida) {
        return new ComidaDto(comida.getId(), comida.getNombre(), comida.getPrecio());
    }

    private Comida toEntity(ComidaDto dto) {
        return new Comida(dto.getId(), dto.getNombre(), dto.getPrecio());
    }
}
