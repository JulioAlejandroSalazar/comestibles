package cl.duoc.comestibles.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComidaDto {

    private String id;
    private String nombre;
    private int precio;
    
}
