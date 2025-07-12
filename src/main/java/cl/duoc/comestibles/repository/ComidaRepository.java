package cl.duoc.comestibles.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import cl.duoc.comestibles.model.Comida;

@Repository
public class ComidaRepository {

    private final List<Comida> comidas = new ArrayList<>();

    public List<Comida> findAll() {
        return comidas;
    }

    public void addComida(Comida comida) {
        comidas.add(comida);
    }

    public Comida findComidaByID(String id) {
        for(Comida comida : comidas) {
            if(comida.getId().equals(id)) {
                return comida;
            }
        }
        return null;
    }

    public void updateComida(String id, Comida updateComida) {
        for(int i = 0; i < comidas.size(); i++) {
            if(comidas.get(i).getId().equals(id)){
                comidas.set(i, updateComida);
                return;
            }
        }
    }

    public void deleteComida(String id) {
        for(int i = 0; i < comidas.size(); i++) {
            if(comidas.get(i).getId().equals(id)) {
                comidas.remove(i);
                return;
            }
        }
    }

    public ComidaRepository() {
        comidas.add(new Comida("1", "pasta", 5000));
        comidas.add(new Comida("2", "sopa", 4500));
        comidas.add(new Comida("3", "carne", 8000));
        comidas.add(new Comida("4", "error", 1000));
    }
    
}
