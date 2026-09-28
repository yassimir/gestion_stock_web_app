package com.gharnata.controller;

import com.gharnata.entity.*;
import com.gharnata.entity.dto.ProdInfo;
import com.gharnata.repository.RepanierItemsSF;
import com.gharnata.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gharnata")
public class FactureController {
    @Autowired
    private ServCommande servCommande;
    @Autowired
    private ServFacture servFacture;
    @Autowired
    private ServSimFac servSimFac;
    @Autowired
    private ServProduit servProduit;
    @Autowired
    private ServClient servClient;
    @Autowired
    private RepanierItemsSF repanierItemsSF;
    @Autowired
    private ServNotification servNotification;
    @Autowired
    private ServPaiement servPaiement;

    @GetMapping("/factures")
    public String factures(Model model){
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/invoiceManagement";
    }

    @PostMapping("/load-prods")
    public String generateFacture(@RequestParam int idCat, HttpServletResponse response,
                                  @RequestParam(required = false) String qt,
                                  @RequestParam(required = false) String pr){
        if (idCat == 0){
            this.servFacture.loadProds(this.servProduit.getProducts(), response, qt, pr);
        } else {
            this.servFacture.loadProds(this.servProduit.getProductsByCat(idCat), response, qt, pr);
        }
        return null;
    }
    @PostMapping("/generateBon")
    public String generateBon(@RequestParam("id") long id, @RequestParam(value = "rap", required = false) String rap, HttpServletResponse response){
        Commande commande = this.servCommande.findCommandById(id);
        List<LigneCommande> lPI = this.servCommande.getLignesByCommande(commande);
        this.servFacture.genererBon(commande, lPI, response, rap);
        return null;
    }
    @GetMapping("/sim-fac")
    public String simFac(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        List<PanierItemsSF> panierItems = this.servSimFac.getPanierItemsByAdm(userId);
        List<ProdInfo> lPI = this.servProduit.getReferences();
        List<Integer> indexs =new ArrayList<>();
        for (int i=1; i<=panierItems.size();i++){
            indexs.add(i);
        }
        servSimFac.checkPanierExi(userId);
        model.addAttribute("prodInfos", lPI);
        model.addAttribute("items",panierItems);
        model.addAttribute("panierSF", this.servSimFac.calculPanierMss(panierItems, userId));
        model.addAttribute("cls", this.servClient.getAllClients());
        model.addAttribute("idxs", indexs);
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/simFac";
    }
    @PostMapping("/sim-fac")
    public String simFac(@RequestParam("ref") String ref, @RequestParam("quant") int quant, RedirectAttributes attributes){
        if(quant<=0){
            attributes.addFlashAttribute("error", "Veuillez choisir une quantité.");
            return"redirect:/gharnata/sim-fac";
        }
        Produit produit = this.servProduit.getProductByRef(ref);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        PanierSFac panierSFac = servSimFac.checkPanierExi(userId);
        if (this.servSimFac.checkItemExist(panierSFac,produit)){
            attributes.addFlashAttribute("success", "La quantité du produit <"+produit.getRef()+"> a été modifiée.");
        }
        PanierItemsSF pannierItemSF = this.servSimFac.plusPItem(produit, panierSFac, quant);
        if (pannierItemSF==null) {
            attributes.addFlashAttribute("error", "Problème lors de l'ajout au panier.");
        }
        return"redirect:/gharnata/sim-fac";
    }
    @PostMapping("/gen-sim-fac")
    public String genSimFac(@RequestParam("iceCl") String ice,@RequestParam("date") String date , @RequestParam("pInfo") String pInfo, RedirectAttributes attributes, HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        PanierSFac panierSFac = servSimFac.checkPanierExi(userId);
        List<PanierItemsSF> lPPI = this.repanierItemsSF.findAllByPanierSFac(panierSFac);
        if (lPPI.isEmpty()){
            attributes.addFlashAttribute("error", "Veuiller choisir des produits.");
            return "redirect:/gharnata/sim-fac";
        }
        SimFacture simFacture = this.servSimFac.saveSiFac(ice, date,pInfo, panierSFac);
        List<LigneSFacture> lSF = this.servSimFac.saveLigneSF(simFacture,panierSFac, lPPI);
        this.servFacture.simulerFacture(simFacture, lSF, response);
        return "redirect:/gharnata/sim-fac";
    }
    @GetMapping("/vider-page")
    public String videPage(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        PanierSFac panierSFac = servSimFac.checkPanierExi(userId);
        List<PanierItemsSF> lPPI = this.repanierItemsSF.findAllByPanierSFac(panierSFac);
        this.servSimFac.videPage(panierSFac, lPPI);
        return "redirect:/gharnata/sim-fac";
    }
    @GetMapping("/delete-item-pasf/{id}")
    public String deleteIt(@PathVariable("id") int id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        this.servSimFac.deleteItemById(id);
        return "redirect:/gharnata/sim-fac";
    }
    @PostMapping("/telecharger-facture")
    public void telechergerFac(@RequestParam("id") Long id, @RequestParam(value = "tmp", required = false) String tmp,HttpServletResponse response) throws Exception {
        Paiement paiement = this.servPaiement.getPaiementById(id);
        Facture facture = this.servFacture.getFactureByPaiement(paiement);
        this.servFacture.genererFacture(facture, tmp, response);
    }
}
