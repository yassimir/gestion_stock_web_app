package com.gharnata.repository;

import com.gharnata.entity.Panier;
import com.gharnata.entity.PanierSFac;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepPanierSFac extends JpaRepository<PanierSFac, Integer> {
    PanierSFac findByAdmName(String userId);
}
