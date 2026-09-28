package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "commande")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private final double tva=0.2;
    @Column(name = "date")
    private String date;
    @Column(name = "paye", columnDefinition = "boolean default false")
    private boolean paye;
    @Column(name = "montant_ht", columnDefinition = "double default 0.0")
    private double montantHT;
    @Column(name = "montant_ttc", columnDefinition = "double default 0.0")
    private double montantTTC;
    @Column(name = "mtva", columnDefinition = "double default 0.0")
    private double mTVA;
    @Column(name = "mtl")
    private String mtl;
    @Column(name = "confirme",columnDefinition = "boolean default false")
    private boolean confirme;
    @Column(name = "date_confirmation")
    private String dateConfirmation;
    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Paiement> paiements;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Facture> factures;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTva() {
        return tva;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPaye() {
        return paye;
    }

    public void setPaye(boolean paye) {
        this.paye = paye;
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

    public double getMTVA() {
        return mTVA;
    }

    public void setMTVA(double mTVA) {
        this.mTVA = mTVA;
    }

    public String getMtl() {
        return mtl;
    }

    public void setMtl(String mtl) {
        this.mtl = mtl;
    }

    public boolean isConfirme() {
        return confirme;
    }

    public void setConfirme(boolean confirme) {
        this.confirme = confirme;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getDateConfirmation() {
        return dateConfirmation;
    }

    public void setDateConfirmation(String dateConfirmation) {
        this.dateConfirmation = dateConfirmation;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }

    public List<Facture> getFactures() {
        return factures;
    }

    public void setFactures(List<Facture> factures) {
        this.factures = factures;
    }
}
