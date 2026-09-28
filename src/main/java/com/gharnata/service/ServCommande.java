package com.gharnata.service;


import com.gharnata.entity.*;
import com.gharnata.entity.dto.ProdQte;
import com.gharnata.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ServCommande {
    @Autowired
    private RepCommande repCommande;
    @Autowired
    private RepLigneCommand repLigneCommand;
    @Autowired
    private RepProduit repProduit;
    @Autowired
    private RePaiement rePayments;
    @Autowired
    private ServFacture servFacture;

    public Commande saveCommand(Client client){
        Commande commande= new Commande();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        commande.setDate(sdf.format(date));
        commande.setClient(client);
        commande.setPaye(false);
        commande.setConfirme(false);
        return this.repCommande.save(commande);
    }
    public Commande saveCommand(Commande commande){
        return this.repCommande.save(commande);
    }
    public int saveLCom(Commande commande, List<PanierItems> lPI){
        int i = 0;
        double montant=0;
        for (PanierItems panierItem : lPI) {
            LigneCommande lc = new LigneCommande();
            lc.setCommande(commande);
            lc.setQuantity(panierItem.getQuantite());
            lc.setProductName(panierItem.getProduit().getName());
            lc.setRef(panierItem.getProduit().getRef());
            lc.setPrixVente(panierItem.getProduit().getPrixVente());
            lc.setMontant(panierItem.getProduit().getPrixVente()* panierItem.getQuantite());
            lc.setProductId(panierItem.getProduit().getId());
            this.repLigneCommand.save(lc);
            i++;
            montant+= lc.getMontant();
        }
        commande.setMontantHT(montant);
        commande.setMTVA(commande.getMontantHT()* commande.getTva());
        commande.setMontantTTC(commande.getMontantHT()+commande.getMTVA());
        int dirhams = (int) commande.getMontantTTC();
        int centimes = (int) Math.round((commande.getMontantHT() - dirhams) * 100);
        String mtl = NombreEnLettres.convertir(dirhams) + " dirhams";
        if (centimes > 0) {
            mtl += " et " + NombreEnLettres.convertir(centimes) + " centimes";
        }
        commande.setMtl(mtl);
        this.repCommande.save(commande);
        return i ;
    }
    public List<Commande> getAllCommands() {
        Sort sort = Sort.by(Sort.Order.desc("id"));
        return this.repCommande.findAll(sort);
    }

    public Commande findCommandById(Long id) {
        return this.repCommande.findById(id).orElse(null);
    }

    public List<LigneCommande> getLignesByCommande(Commande commande) {
        return this.repLigneCommand.findAllByCommande(commande);
    }

    public LigneCommande getLigneComandById(long id){
        return this.repLigneCommand.findById(id).orElse(new LigneCommande());
    }
    public long deleteLigneComand(long id){
        LigneCommande lc = getLigneComandById(id);
        Commande commande = findCommandById(lc.getCommande().getId());
        /*Compte cmp = this.servClient.getCmptById(commande.getClient().getCompte().getId());
        cmp.setCredit(cmp.getCredit()-lc.getMontant());
        this.servClient.saveCmpt(cmp);*/
        commande.setMontantHT(commande.getMontantHT()-lc.getMontant());
        commande.setMTVA(commande.getMontantHT() * commande.getTva());
        commande.setMontantTTC(commande.getMontantHT()+commande.getMTVA());
        this.repCommande.save(commande);
        this.repLigneCommand.deleteById(id);
        return commande.getId();
    }

    public void deleteComand(long id) {
        Commande c = findCommandById(id);
        List<LigneCommande> ligneCommandes = this.repLigneCommand.findAllByCommande(c);
        this.repLigneCommand.deleteAll(ligneCommandes);
        this.repCommande.delete(c);
    }
    public void addOneLCom(Commande c, Produit p, int qte) {
        List<LigneCommande> llc = this.repLigneCommand.findAllByCommande(c);
        for(LigneCommande l :llc){
            if(Objects.equals(l.getRef(), p.getRef())){
                return;
            }
        }
        LigneCommande lc = new LigneCommande();
        lc.setQuantity(qte);
        lc.setCommande(c);
        lc.setRef(p.getRef());
        lc.setProductName(p.getName());
        lc.setProductId(p.getId());
        lc.setPrixVente(p.getPrixVente());
        lc.setMontant(p.getPrixVente()*qte);
        this.repLigneCommand.save(lc);
        c.setMontantHT(c.getMontantHT()+lc.getMontant());
        c.setMTVA(c.getMTVA()+(c.getTva()*lc.getMontant()));
        c.setMontantTTC(c.getMontantHT()+c.getMTVA());
        this.repCommande.save(c);
        /*Compte cmp = this.servClient.getCmptById(c.getClient().getCompte().getId());
        cmp.setCredit(cmp.getCredit()+lc.getMontant());
        this.servClient.saveCmpt(cmp);*/
    }
    private List<LigneCommande> getLigneCommandesByProduit(Produit produit) {
        return this.repLigneCommand.findAllByProductId(produit.getId());
    }

    public int checkProductInStock(Produit produit) {
        List<LigneCommande> lLC = this.getLigneCommandesByProduit(produit);
        int qteResrv = 0;
        for (LigneCommande lc : lLC){
            if(!lc.getCommande().isConfirme()){
                qteResrv += lc.getQuantity();
            }
        }
        return produit.getQuantite() - qteResrv;
    }

    public List<Commande> findCommandProduitEx(Produit produit) {
        List<LigneCommande>  lLC = this.repLigneCommand.findAllByProductId(produit.getId());
        List<Commande> commandId = new ArrayList<>();
        for (LigneCommande lc : lLC){
            if (!lc.getCommande().isConfirme()){
                commandId.add(lc.getCommande());
            }
        }
        return commandId;
    }
    public Paiement savePaym(Client client, double montant, String description){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Paiement payment = new Paiement();
        payment.setClient(client);
        payment.setMontant(montant);
        payment.setDatePay(sdf.format(new Date()));
        payment.setDescription(description);
        return this.rePayments.save(payment);
    }

    public List<LigneFacture> persFactItems(Facture fac, Commande commande) {
        int dirhams = (int) fac.getMontantTTC();
        int centimes = (int) Math.round((fac.getMontantTTC() - dirhams) * 100);
        String mtl = NombreEnLettres.convertir(dirhams) + " dirhams";
        if (centimes > 0) {
            mtl += " et " + NombreEnLettres.convertir(centimes) + " centimes";
        }
        fac.setMtl(mtl);
        this.servFacture.saveFac(fac);
        List<LigneCommande> lines = this.repLigneCommand.findAllByCommande(commande);
        List<ProdQte> lPQ = new ArrayList<>();
        double mt= 0;
        int i=0;
        while (mt < fac.getMontantHT() && i<500) {
            int j=0;
            while (mt < fac.getMontantHT() && j < lines.size()) {
                if (mt + lines.get(j).getPrixVente() <= fac.getMontantHT()) {
                    ProdQte pq = new ProdQte();
                    pq.setLigneCommande(lines.get(j));
                    pq.setQte(pq.getQte() + 1);
                    lPQ.add(pq);
                    mt += lines.get(j).getPrixVente();
                }
                j++;
            }
            i++;
        }

        if(mt<fac.getMontantHT()){
            double diff = fac.getMontantHT()- mt;
            List<Produit> lP = this.repProduit.findByPrixVenteLessThanEqual(diff);
            i=0;
            while(mt<fac.getMontantHT() && i<1000){
                int j=0;
                while (mt < fac.getMontantHT() && j < lP.size()) {
                    if (mt + lP.get(j).getPrixVente()<= fac.getMontantHT()){
                        LigneCommande lt = new LigneCommande();
                        ProdQte pq = new ProdQte();
                        lt.setQuantity(1);
                        lt.setRef(lP.get(j).getRef());
                        lt.setProductName(lP.get(j).getName());
                        lt.setProductId(lP.get(j).getId());
                        lt.setMontant(lP.get(j).getPrixVente());
                        lt.setPrixVente(lP.get(j).getPrixVente());
                        pq.setLigneCommande(lt);
                        pq.setQte(pq.getQte()+1);
                        System.out.println(pq.getLigneCommande().getProductName());
                        lPQ.add(pq);
                        mt+=lP.get(j).getPrixVente();
                    }
                    j++;
                }
                i++;
            }
        }
        List<LigneFacture> lLLC = new ArrayList<>();
        for (ProdQte pq :lPQ){
            boolean existe = false;
            for (LigneFacture l : lLLC){
                if(l.getProductId()==pq.getLigneCommande().getProductId()){
                    l.setQuantity(l.getQuantity()+ pq.getQte());
                    l.setMontant(l.getPrixVente()*l.getQuantity());
                    existe = true;
                    break;
                }
            }
            if (existe) {
                continue; // on saute la création de nouvelle ligne
            }
            LigneFacture lf = new LigneFacture();
            lf.setMontant(pq.getLigneCommande().getPrixVente() * pq.getQte());
            lf.setFacture(fac);
            lf.setQuantity(pq.getQte());
            lf.setRef(pq.getLigneCommande().getRef());
            lf.setPrixVente(pq.getLigneCommande().getPrixVente());
            lf.setProductId(pq.getLigneCommande().getProductId());
            lf.setProductName(pq.getLigneCommande().getProductName());
            lLLC.add(lf);
        }
        for (LigneFacture lf : lLLC){
            this.servFacture.saveLF(lf);
        }
        return lLLC;
    }


    public List<Commande> getLimitByClient(Client client) {
        return this.repCommande.findTop7ByClientOrderByIdDesc(client);
    }

    public List<Commande> getCommandsByClient(Client client) {
        return this.repCommande.findAllByClientOrderByIdDesc(client);
    }
}
