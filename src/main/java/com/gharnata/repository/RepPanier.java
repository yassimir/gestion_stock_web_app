package com.gharnata.repository;

import com.gharnata.entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepPanier extends JpaRepository<Panier, Integer> {
    Panier findByAdmName(String admname);
}
