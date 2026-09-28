package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "panierItemsSF")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanierItemsSF {
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
    @JoinColumn(name = "idPanierSF", referencedColumnName = "id")
    private PanierSFac panierSFac;

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

    public PanierSFac getPanierSFac() {
        return panierSFac;
    }

    public void setPanierSFac(PanierSFac panierSFac) {
        this.panierSFac = panierSFac;
    }
}
