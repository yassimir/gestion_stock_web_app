package com.gharnata.service;

import com.gharnata.entity.Client;
import com.gharnata.entity.Commande;
import com.gharnata.entity.Facture;
import com.gharnata.entity.Paiement;
import com.gharnata.repository.RePaiement;
import com.gharnata.repository.RepFacture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ServPaiement {
    @Autowired
    private RePaiement rePaiement;

    public Paiement effectuerPaiement(Commande commande, double montantPaye, String modepai, String description) {
        Paiement paiement = new Paiement();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        paiement.setMontant(montantPaye);
        paiement.setDatePay(sdf.format((new Date())));
        paiement.setModPai(modepai);
        paiement.setCommande(commande);
        paiement.setClient(commande.getClient());
        paiement.setDescription(description);
        return rePaiement.save(paiement);
    }

    public void savePaie(Paiement paiement) {
        this.rePaiement.save(paiement);
    }

    public List<Paiement> getPaiementsByCommande(Commande commande) {
        return this.rePaiement.findAllByCommande(commande);
    }
    public double calculateTotalPaiement(Commande commande) {
        List<Paiement> listPai = this.getPaiementsByCommande(commande);
        double total=0;
        for (Paiement p : listPai){
            total += p.getMontant();
        }
        return total;
    }

    public List<Paiement> getLimitByClient(Client client) {
        return this.rePaiement.findTop7ByClientOrderByIdDesc(client);
    }
    public List<Paiement> getPaymentsByClient(Client client) {
        return this.rePaiement.findAllByClientOrderByIdDesc(client);
    }

    public Paiement getPaiementById(Long id) {
        return this.rePaiement.findById(id).orElse(null);
    }
}
