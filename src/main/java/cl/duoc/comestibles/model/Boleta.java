package cl.duoc.comestibles.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Boleta {

    private String id;
    private List<Comida> comidas;
    private int total;

    public Boleta(List<Comida> comidas) {
        this.id = java.util.UUID.randomUUID().toString();
        this.comidas = comidas;
        this.total = comidas.stream().mapToInt(Comida::getPrecio).sum();
    }
}
