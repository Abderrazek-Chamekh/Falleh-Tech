package tn.esprit.entities;

public class PanierItem {
    private Produit produit;
    private int quantite;

    public PanierItem(Produit produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public void updateTotal() {
        // méthode vide ici car getTotal() est dynamique,
        // mais utile pour forcer un refresh visuel dans JavaFX
    }

    public double getTotal() {
        return produit.getPrix().doubleValue() * quantite;
    }
}
