package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facture")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {
    @Id
    private long id;
    @Column(name = "date")
    private String date;
    @Column(name = "montant_ht", columnDefinition = "double default 0.0")
    private double montantHT;
    @Column(name = "montant_ttc", columnDefinition = "double default 0.0")
    private double montantTTC;
    @Column(name = "mtva", columnDefinition = "double default 0.0")
    private double mTVA;
    @Column(name = "mtl")
    private String mtl;
    @ManyToOne
    private Commande commande;
    @ManyToOne
    private Client client;
    @OneToOne
    private Paiement paiement;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getMontantHT() {
        return montantHT;
    }

    public void setMontantHT(double montantHT) {
        this.montantHT = montantHT;
    }

    public double getMontantTTC() {
        return montantTTC;
    }

    public void setMontantTTC(double montantTTC) {
        this.montantTTC = montantTTC;
    }

    public String getMtl() {
        return mtl;
    }

    public void setMtl(String mtl) {
        this.mtl = mtl;
    }

    public double getmTVA() {
        return mTVA;
    }

    public void setmTVA(double mTVA) {
        this.mTVA = mTVA;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Paiement getPaiement() {
        return paiement;
    }

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
    }
}
