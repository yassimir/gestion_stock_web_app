package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ligneSFacture")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneSFacture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "quantite")
    private int quantity;
    @JoinColumn(name = "idProduit")
    private long productId;
    @Column(name = "reference")
    private String ref;
    @Column(name = "nom_produit")
    private String productName;
    @Column(name = "prix_vente")
    private double prixVente;
    @Column(name = "montant", columnDefinition = "double default 0.0")
    private double montant;
    @ManyToOne
    @JoinColumn(name = "idSFacture")
    private SimFacture simFacture;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public SimFacture getSimFacture() {
        return simFacture;
    }

    public void setSimFacture(SimFacture simFacture) {
        this.simFacture = simFacture;
    }
}
