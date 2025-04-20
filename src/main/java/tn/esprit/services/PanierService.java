package tn.esprit.services;

import tn.esprit.entities.Produit;

import java.util.HashMap;
import java.util.Map;

public class PanierService {

    private static final PanierService INSTANCE = new PanierService();

    private final Map<Produit, Integer> panier = new HashMap<>();

    private PanierService() {} // 🔒 Constructeur privé

    public static PanierService getInstance() {
        return INSTANCE;
    }

    public void ajouterProduit(Produit produit, int quantite) {
        panier.merge(produit, quantite, Integer::sum);
    }

    public void supprimerProduit(Long produitId) {
        panier.keySet().removeIf(p -> p.getId().equals(produitId));
    }

    public Map<Produit, Integer> getPanier() {
        return panier;
    }

    public double getTotal() {
        return panier.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrix().doubleValue() * e.getValue())
                .sum();
    }

    public void vider() {
        panier.clear();
    }
    public boolean contient(Produit produit) {
        return panier.keySet().stream()
                .anyMatch(p -> p.getId().equals(produit.getId()));
    }

}
