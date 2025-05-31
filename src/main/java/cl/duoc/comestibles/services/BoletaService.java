package cl.duoc.comestibles.services;

import java.util.List;

public interface BoletaService {
    String generarYSubirBoleta(List<String> comidaIds);
    byte[] descargarBoleta(String boletaId);
    List<String> listarBoletas();
    void eliminarBoleta(String boletaId);
}

