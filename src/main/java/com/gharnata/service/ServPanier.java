package com.gharnata.service;

import com.gharnata.entity.Panier;
import com.gharnata.entity.PanierItems;
import com.gharnata.entity.Produit;
import com.gharnata.repository.RePanItem;
import com.gharnata.repository.RepPanier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ServPanier {
    @Autowired
    private RepPanier repPanier;
    @Autowired
    private RePanItem rePanItem;

    public Panier checkPanierExi(String admname){
        Panier panier = this.repPanier.findByAdmName(admname);
        if (panier==null){
            Panier p = new Panier();
            p.setAdmName(admname);
            this.repPanier.save(p);
            return p;
        }
        return panier;
    }
    public PanierItems addPItem(Produit produit, Panier panier, int quant){
        PanierItems panierItem = new PanierItems();
        panierItem.setPanier(panier);
        panierItem.setProduit(produit);
        panierItem.setQuantite(quant);
        panierItem.setMontant(produit.getPrixVente()*quant);
        return this.rePanItem.save(panierItem);
    }
    public boolean checkItemExist(Panier panier, Produit produit){
        List<PanierItems> lsPI = this.rePanItem.findAllByPanier(panier);
        if (lsPI.isEmpty()){
            return false;
        }
        for (PanierItems panierItem : lsPI){
            if(Objects.equals(panierItem.getProduit().getRef(), produit.getRef())){
                return true;
            }
        }
        return false;
    }

    public List<PanierItems> getPanierItemsByAdm(String userId) {
        Panier panier = this.repPanier.findByAdmName(userId);
        return this.rePanItem.findAllByPanier(panier);

    }
    public void deleteItemById(int id){
        this.rePanItem.deleteById(id);
    }

    public void deleteItemsByUserId(String userId) {
        Panier panier = this.repPanier.findByAdmName(userId);
        List<PanierItems> lPI = this.rePanItem.findAllByPanier(panier);
        this.rePanItem.deleteAll(lPI);
    }
    public double calculPrix(List<PanierItems> lPI) {
        double mht = 0;
        for (PanierItems panierItem : lPI) {
            mht+=panierItem.getProduit().getPrixVente()*panierItem.getQuantite();
        }

        return mht;
    }
    public double calculPanierMss(List<PanierItems> lPI) {
        double mht = 0;
        for (PanierItems panierItem : lPI) {
            mht+=panierItem.getProduit().getPrixVente()*panierItem.getQuantite();
        }
        return mht;
    }

    public void checkProductExi(Produit produit) {
        List<PanierItems> lPI = this.rePanItem.findByProduit(produit);
        if (!lPI.isEmpty()){
            this.rePanItem.deleteAll(lPI);
        }
    }

    public void checkProductsExi(List<Produit> prods) {
        List<PanierItems> lPI = this.rePanItem.findByProduitIn(prods);
        if (!lPI.isEmpty()){
            this.rePanItem.deleteAll(lPI);
        }
    }
}
