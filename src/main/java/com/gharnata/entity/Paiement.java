package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "montant")
    private double montant;
    @Column(name = "date_pay")
    private String datePay;
    @Column(name = "description")
    private String description;
    @Column(name = "mod_pai")
    private String modPai;
    @ManyToOne
    private Commande commande;

    @OneToOne(mappedBy = "paiement", cascade = CascadeType.ALL)
    private Facture facture;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDatePay() {
        return datePay;
    }

    public void setDatePay(String datePay) {
        this.datePay = datePay;
    }

    public String getModPai() {
        return modPai;
    }

    public void setModPai(String modPai) {
        this.modPai = modPai;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
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
}
