package cl.duoc.comestibles.services;

import java.util.List;

import cl.duoc.comestibles.model.Boleta;

public interface BoletaService {
    Boleta generarYSubirBoleta(List<String> comidaIds);
    byte[] descargarBoleta(String boletaId);
    List<Boleta> listarBoletas();
    void eliminarBoleta(String boletaId);
}

