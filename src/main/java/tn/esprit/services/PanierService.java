package tn.esprit.services;

import tn.esprit.entities.Produit;

import java.util.HashMap;
import java.util.Map;

public class PanierService {

    private static final PanierService INSTANCE = new PanierService();
    private Double totalAvecRemise = null;
    private boolean remiseAppliquee = false;

    private final Map<Produit, Integer> panier = new HashMap<>();

    private PanierService() {}

    public static PanierService getInstance() {
        return INSTANCE;
    }

    public void ajouterProduit(Produit produit, int quantite) {
        panier.merge(produit, quantite, Integer::sum);
        resetRemise(); // ❗ Invalide la remise si panier change
    }

    public void supprimerProduit(Long produitId) {
        panier.keySet().removeIf(p -> p.getId().equals(produitId));
        resetRemise(); // ❗ Invalide la remise si panier change
    }

    public Map<Produit, Integer> getPanier() {
        return panier;
    }

    public double getTotalSansRemise() {
        return panier.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrix().doubleValue() * e.getValue())
                .sum();
    }

    public double getTotal() {
        return totalAvecRemise != null ? totalAvecRemise : getTotalSansRemise();
    }

    public void vider() {
        panier.clear();
        resetRemise();
    }

    public boolean contient(Produit produit) {
        return panier.keySet().stream().anyMatch(p -> p.getId().equals(produit.getId()));
    }

    public void appliquerRemise(double pourcentage) {
        if (!remiseAppliquee) {
            double total = getTotalSansRemise();
            double remise = total * pourcentage;
            this.totalAvecRemise = total - remise;
            this.remiseAppliquee = true;
        }
    }

    public boolean isRemiseAppliquee() {
        return remiseAppliquee;
    }

    public void resetRemise() {
        this.totalAvecRemise = null;
        this.remiseAppliquee = false;
    }
}
