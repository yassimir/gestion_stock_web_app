package com.gharnata.repository;

import com.gharnata.entity.Facture;
import com.gharnata.entity.LigneFacture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepLigneFac  extends JpaRepository<LigneFacture, Long> {
    List<LigneFacture> findAllByFacture(Facture facture);

}
