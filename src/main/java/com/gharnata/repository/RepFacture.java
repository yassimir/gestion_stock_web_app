package com.gharnata.repository;

import com.gharnata.entity.Client;
import com.gharnata.entity.Commande;
import com.gharnata.entity.Facture;
import com.gharnata.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepFacture extends JpaRepository<Facture, Long> {

    Facture findByCommande(Commande commande);

    Facture findByPaiement(Paiement paiement);
}
