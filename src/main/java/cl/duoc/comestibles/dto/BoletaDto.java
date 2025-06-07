package cl.duoc.comestibles.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoletaDto {
    private String id;
    private List<ComidaDto> comidas;
    private int total;

    public BoletaDto(List<ComidaDto> comidas) {
        this.id = java.util.UUID.randomUUID().toString();
        this.comidas = comidas;
        this.total = comidas.stream().mapToInt(ComidaDto::getPrecio).sum();
    }
    
}
