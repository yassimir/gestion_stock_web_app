package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "simFacture")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimFacture {
    @Id
    private long id;
    private final double tva=0.2;
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
    @Column(name = "modPai")
    private String modPai;
    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

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

    public double getmTVA() {
        return mTVA;
    }

    public void setmTVA(double mTVA) {
        this.mTVA = mTVA;
    }

    public String getMtl() {
        return mtl;
    }

    public void setMtl(String mtl) {
        this.mtl = mtl;
    }

    public String getModPai() {
        return modPai;
    }

    public void setModPai(String modPai) {
        this.modPai = modPai;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
