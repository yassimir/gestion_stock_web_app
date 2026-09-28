package com.gharnata.repository;

import com.gharnata.entity.Panier;
import com.gharnata.entity.PanierItems;
import com.gharnata.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RePanItem extends JpaRepository<PanierItems, Integer> {
    List<PanierItems> findAllByPanier(Panier panier);
    PanierItems findByPanierAndAndProduit(Panier panier, Produit produit);

    List<PanierItems> findByProduit(Produit produit);
    List<PanierItems> findByProduitIn(List<Produit> prods);
}
