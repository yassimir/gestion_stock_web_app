package com.gharnata.entity.dto;

import com.gharnata.entity.LigneCommande;
import com.gharnata.entity.Produit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class ProdQte {
    Long id;
    LigneCommande ligneCommande;
    int qte = 0;

    public ProdQte(Long id, LigneCommande ligneCommande, int qte) {
        this.id = id;
        this.ligneCommande = ligneCommande;
        this.qte = qte;
    }
    public ProdQte(){}

    public LigneCommande getLigneCommande() {
        return ligneCommande;
    }

    public void setLigneCommande(LigneCommande ligneCommande) {
        this.ligneCommande = ligneCommande;
    }

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }
}
