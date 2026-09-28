package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorie")
@AllArgsConstructor
@Builder
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "lib")
    private String lib;

    public Categorie(String lib) {
        this.lib = lib;
    }
    public Categorie() {}


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLib() {
        return lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
    }
}
