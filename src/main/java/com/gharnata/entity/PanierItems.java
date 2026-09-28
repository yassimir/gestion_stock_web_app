package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "panierItems")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanierItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "idProduit", referencedColumnName = "id")
    private Produit produit;
    @Column(name = "quantite")
    private int quantite;
    @Column(name = "montant", columnDefinition = "double default 0.0")
    private double montant;
    @ManyToOne
    @JoinColumn(name = "idPanier", referencedColumnName = "id")
    private Panier panier;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Panier getPanier() {
        return panier;
    }

    public void setPanier(Panier panier) {
        this.panier = panier;
    }
}
