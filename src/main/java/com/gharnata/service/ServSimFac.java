package com.gharnata.service;


import com.gharnata.entity.*;
import com.gharnata.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ServSimFac {
    @Autowired
    private RepSimFacture repsimFacture;
    @Autowired
    private RepPanierSFac repPanierSFac;
    @Autowired
    private RepLigneSFac repLigneSFac;
    @Autowired
    private RepanierItemsSF repanierItemsSF;
    @Autowired
    private ServClient servClient;
    @Autowired
    private NumeroFactureService numeroFactureService;

    public List<PanierItemsSF> getPanierItemsByAdm(String userId) {
        PanierSFac panierSFac = this.repPanierSFac.findByAdmName(userId);
        return this.repanierItemsSF.findAllByPanierSFac(panierSFac);

    }
    public PanierSFac calculPanierMss(List<PanierItemsSF> lPI, String userId) {
        double mht = 0;
        PanierSFac panierSFac = this.repPanierSFac.findByAdmName(userId);
        for (PanierItemsSF panierItemsSF : lPI) {
            mht+=panierItemsSF.getProduit().getPrixVente()*panierItemsSF.getQuantite();
        }
        panierSFac.setMontantHT(mht);
        panierSFac.setmTVA(mht*panierSFac.getTva());
        panierSFac.setMontantTTC(panierSFac.getMontantHT()+panierSFac.getmTVA());
        return this.repPanierSFac.save(panierSFac);
    }

    public PanierSFac checkPanierExi(String userId) {
        PanierSFac panierSFac = this.repPanierSFac.findByAdmName(userId);
        if (panierSFac==null){
            PanierSFac p = new PanierSFac();
            p.setAdmName(userId);
            this.repPanierSFac.save(p);
            return p;
        }
        return panierSFac;
    }

    public boolean checkItemExist(PanierSFac panierSFac, Produit produit) {
        List<PanierItemsSF> lsPI = this.repanierItemsSF.findAllByPanierSFac(panierSFac);
        if (lsPI.size()==0){
            return false;
        }
        for (PanierItemsSF panierItemsSF : lsPI){
            if(Objects.equals(panierItemsSF.getProduit().getRef(), produit.getRef())){
                this.repanierItemsSF.delete(panierItemsSF);
                return true;
            }
        }
        return false;
    }

    public PanierItemsSF plusPItem(Produit produit, PanierSFac panierSFac, int quant) {
        PanierItemsSF panierItemsSF = new PanierItemsSF();
        panierItemsSF.setPanierSFac(panierSFac);
        panierItemsSF.setProduit(produit);
        panierItemsSF.setQuantite(quant);
        panierItemsSF.setMontant(produit.getPrixVente()*quant);
        return this.repanierItemsSF.save(panierItemsSF);
    }

    public SimFacture saveSiFac(String ice, String date, String pInfo, PanierSFac panierSFac) {
        SimFacture simFacture = new SimFacture();
        simFacture.setId(numeroFactureService.genererNouveauNumero());
        simFacture.setClient(this.servClient.getClientByIce(ice));
        simFacture.setDate(date);
        int dirhams = (int) panierSFac.getMontantTTC();
        int centimes = (int) Math.round((panierSFac.getMontantHT() - dirhams) * 100);
        String mtl = NombreEnLettres.convertir(dirhams) + " dirhams";
        if (centimes > 0) {
            mtl += " et " + NombreEnLettres.convertir(centimes) + " centimes";
        }
        simFacture.setMtl(mtl);
        simFacture.setModPai(pInfo);
        simFacture.setMontantHT(panierSFac.getMontantHT());
        simFacture.setmTVA(panierSFac.getmTVA());
        simFacture.setMontantTTC(panierSFac.getMontantTTC());
        return this.repsimFacture.save(simFacture);
    }

    public List<LigneSFacture> saveLigneSF(SimFacture simFacture, PanierSFac panierSFac, List<PanierItemsSF> lPPI) {
        List<LigneSFacture> lSFF = new ArrayList<>();
        for (PanierItemsSF pI : lPPI){
            LigneSFacture lsf = new LigneSFacture();
            lsf.setRef(pI.getProduit().getRef());
            lsf.setMontant(pI.getMontant());
            lsf.setQuantity(pI.getQuantite());
            lsf.setProductName(pI.getProduit().getName());
            lsf.setPrixVente(pI.getProduit().getPrixVente());
            lsf.setProductId(pI.getProduit().getId());
            lsf.setSimFacture(simFacture);
            lSFF.add(this.repLigneSFac.save(lsf));
        }
        this.repanierItemsSF.deleteAll(lPPI);
        this.repPanierSFac.deleteById(panierSFac.getId());
        return lSFF;
    }
    public void videPage(PanierSFac panierSFac, List<PanierItemsSF> lPPI){
        this.repanierItemsSF.deleteAll(lPPI);
        this.repPanierSFac.deleteById(panierSFac.getId());
    }

    public void deleteItemById(int id) {
        this.repanierItemsSF.deleteById(id);
    }
}
