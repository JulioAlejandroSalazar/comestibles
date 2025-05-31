package cl.duoc.comestibles.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.duoc.comestibles.model.Comida;
import cl.duoc.comestibles.services.ComidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/comida")
@RequiredArgsConstructor
public class ComidaController {

    private final ComidaService comidaService;

    @GetMapping
    public ResponseEntity<List<Comida>> getAllComida() {
        List<Comida> comidas = comidaService.getAllComidas();
        if(comidas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comidas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comida> getComidaById(@PathVariable String id) {
        Comida comida = comidaService.getComidaById(id);
        if(comida == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comida);
    }
    

    @PostMapping
    public ResponseEntity<Void> createComidaEntity(@RequestBody Comida comida) {
        comidaService.createComida(comida);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comida> updateComida(@PathVariable String id, @RequestBody Comida comida) {
        if(!id.equals(comida.getId()) ||
            comidaService.getComidaById(id).equals(comida) ||
            comidaService.getComidaById(id) == null) {
                return ResponseEntity.badRequest().build();
            }
        comidaService.updateComida(id, comida);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComida(@PathVariable String id) {
        if(comidaService.getComidaById(id) == null) {
            return ResponseEntity.badRequest().build();
        }
        comidaService.deleteComida(id);
        return ResponseEntity.noContent().build();
    }
    
}
