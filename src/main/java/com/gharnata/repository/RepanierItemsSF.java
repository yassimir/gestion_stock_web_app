package com.gharnata.repository;

import com.gharnata.entity.PanierItemsSF;
import com.gharnata.entity.PanierSFac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepanierItemsSF extends JpaRepository<PanierItemsSF, Integer> {

    List<PanierItemsSF> findAllByPanierSFac(PanierSFac panierSFac);

    void deleteAllByPanierSFac(PanierSFac panierSFac);
}
