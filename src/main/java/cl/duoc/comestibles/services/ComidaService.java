package cl.duoc.comestibles.services;

import java.util.List;
import org.springframework.stereotype.Service;
import cl.duoc.comestibles.model.Comida;

@Service
public interface ComidaService {
    List<Comida> getAllComidas();
    void createComida(Comida comida);
    Comida getComidaById(String id);
    void updateComida(String id, Comida comida);
    void deleteComida(String id);
}
