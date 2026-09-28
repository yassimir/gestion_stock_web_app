package com.gharnata.repository;

import com.gharnata.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepCategory extends JpaRepository<Categorie, Integer> {
    Categorie findByLib(String cat);
    boolean existsByLib(String nom);
}
