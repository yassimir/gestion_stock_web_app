package com.gharnata.repository;

import com.gharnata.entity.Categorie;
import com.gharnata.entity.Produit;
import com.gharnata.entity.dto.ProductDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RepProduit extends JpaRepository<Produit, Long> {
    Produit findByRef(String ref);

    List<Produit> findAllByCategorie(Categorie c);
    @Query("SELECT new com.gharnata.entity.dto.ProductDTO(p.name, p.ref) FROM Produit p")
    List<ProductDTO> findAllProductDTO();

    List<Produit> findTop18ByOrderByIdDesc();

    List<Produit> findByPrixVenteLessThanEqual(double prixVente);

    @Query("""
    SELECT p
    FROM Produit p
    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(p.ref) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY p.name
""")
    List<Produit> searchProducts(@Param("query") String query);
}
