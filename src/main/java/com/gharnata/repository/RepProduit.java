package com.gharnata.repository;

import com.gharnata.entity.Categorie;
import com.gharnata.entity.Produit;
import com.gharnata.entity.dto.ProdInfo;
import com.gharnata.entity.dto.ProductDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepProduit extends JpaRepository<Produit, Long> {
    Produit findByRef(String ref);
    @Query("SELECT NEW com.gharnata.entity.dto.ProdInfo(p.name, p.ref) FROM Produit p")
    List<ProdInfo> findRef();

    List<Produit> findAllByCategorie(Categorie c);
    @Query("SELECT new com.gharnata.entity.dto.ProductDTO(p.name, p.ref) FROM Produit p")
    List<ProductDTO> findAllProductDTO();

    List<Produit> findTop18ByOrderByIdDesc();

    List<Produit> findByPrixVenteLessThanEqual(double prixVente);
}
