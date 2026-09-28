package com.gharnata.repository;

import com.gharnata.entity.Commande;
import com.gharnata.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepLigneCommand extends JpaRepository<LigneCommande, Long> {
    List<LigneCommande> findAllByCommande(Commande commande);

    List<LigneCommande> findAllByProductId(long id);
}
