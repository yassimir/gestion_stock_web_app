package com.gharnata.controller;

import com.gharnata.entity.Commande;
import com.gharnata.entity.Panier;
import com.gharnata.entity.PanierItems;
import com.gharnata.entity.Produit;
import com.gharnata.service.ServCommande;
import com.gharnata.service.ServNotification;
import com.gharnata.service.ServPanier;
import com.gharnata.service.ServProduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/gharnata")
public class PanierController {
    @Autowired
    private ServPanier servPanier;
    @Autowired
    private ServProduit servProduit;
    @Autowired
    private ServNotification servNotification;
    @Autowired
    private ServCommande servCommande;

    @GetMapping("/show-panier")
    public String showPanier(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        List<PanierItems> panierItems = this.servPanier.getPanierItemsByAdm(userId);
        model.addAttribute("items", panierItems );
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/panier";
    }
    @PostMapping("/addToPanier")
    public String addToPanier(@RequestParam("id") Long id, @RequestParam("quant") int quant, RedirectAttributes attributes){
        if(quant<=0){
            attributes.addFlashAttribute("error", "Veuillez choisir une quantité.");
            return"redirect:/gharnata/product-details/"+id;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        Panier panier = servPanier.checkPanierExi(userId);
        Produit produit = this.servProduit.getProductById(id);
        if (this.servPanier.checkItemExist(panier,produit)){
            attributes.addFlashAttribute("error", "Ce produit existe déjà dans le panier.");
            return"redirect:/gharnata/product-details/"+id;
        }
        PanierItems panierItem = this.servPanier.addPItem(produit, panier, quant);
        if (panierItem==null){
            attributes.addFlashAttribute("error", "Problème lors de l'ajout au panier.");
        }else{
            attributes.addFlashAttribute("success", quant + " "+produit.getName() +" ajouté(s) au panier");
        }
        return"redirect:/gharnata/product-details/"+id;
    }
    @PostMapping("/addToPanier-m")
    public String addToPanierM(@RequestParam("productId") Long productId, @RequestParam("quant") int quant, RedirectAttributes attributes){
        if(quant<=0){
            attributes.addFlashAttribute("error", "Veuillez choisir une quantité.");
            return"redirect:/gharnata/pass-mass";
        }
        Produit produit = this.servProduit.getProductById(productId);
        if(produit==null){
            attributes.addFlashAttribute("error", "Vérifiez la référence : ");
            return"redirect:/gharnata/pass-mass";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        Panier panier = servPanier.checkPanierExi(userId);
        if (this.servPanier.checkItemExist(panier,produit)){
            attributes.addFlashAttribute("error", "Ce produit existe déjà dans le panier.");
            return"redirect:/gharnata/pass-mass";
        }

        int qteDispo = this.servCommande.checkProductInStock(produit);
        if (qteDispo==0){
            attributes.addFlashAttribute("error", "Le produit " + produit.getRef() + " est actuellement en rupture de stock.");
            attributes.addFlashAttribute("qteEnHold",0);
            return"redirect:/gharnata/pass-mass";
        }
        if (quant>qteDispo){
            int qteEnHold = produit.getQuantite()-qteDispo;
            List<Commande> lcP = this.servCommande.findCommandProduitEx(produit);
            attributes.addFlashAttribute("error", "Il existe juste "+qteDispo+" Pièce(s) de cette référence : "+produit.getRef()+ " en stock.");
            attributes.addFlashAttribute("qteEnHold",qteEnHold);
            attributes.addFlashAttribute("lcP", lcP);
            return"redirect:/gharnata/pass-mass";
        }
        PanierItems panierItem = this.servPanier.addPItem(produit, panier, quant);
        if (panierItem==null) {
            attributes.addFlashAttribute("error", "Problème lors de l'ajout au panier.");
        }
        return"redirect:/gharnata/pass-mass";
    }
    @GetMapping("/delete-item-panier/{id}")
    public String deleteItem(@PathVariable("id") int id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        this.servPanier.deleteItemById(id);
        return "redirect:/gharnata/show-panier";
    }
    @GetMapping("/delete-item-pa/{id}")
    public String deleteIt(@PathVariable("id") int id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        this.servPanier.deleteItemById(id);
        return "redirect:/gharnata/pass-mass";
    }
    @GetMapping("/vider-panier/{id}")
    public String deleteIviderPaniert(@PathVariable int id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        this.servPanier.deleteItemsByUserId(userId);
        if(id==1){
            return "redirect:/gharnata/pass-mass";
        }
        return "redirect:/gharnata/show-panier";
    }

}
