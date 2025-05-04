package tn.esprit.services;

import java.util.List;
import tn.esprit.entities.Candidature;

public interface services<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);
    List<T> getAll();

    // Optional method for candidature only
    default void ajouter(Candidature c, int offreId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
