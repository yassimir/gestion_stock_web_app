package com.gharnata.controller;
import com.gharnata.entity.*;
import com.gharnata.service.*;
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
public class CommandeController {
    @Autowired
    private ServClient servClient;
    @Autowired
    private ServPanier servPanier;
    @Autowired
    private ServProduit servProduit;
    @Autowired
    private ServCommande servCommande;
    @Autowired
    private ServNotification servNotification;
    @Autowired
    private ServPaiement servPaie;
    @Autowired
    private ServFacture servFacture;

    @GetMapping("/show-commands")
    public String showCommande(Model model) {
        List<Commande> commandeList = this.servCommande.getAllCommands();
        model.addAttribute("commands", commandeList);
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/showCommandes";
    }

    @GetMapping("/command-details/{id}")
    public String commandeDetails(@PathVariable("id") Long id, Model model) {
        Commande commande = this.servCommande.findCommandById(id);
        if (commande == null) {
            return "redirect:/error";
        }
        double total= this.servPaie.calculateTotalPaiement(commande);
        List<LigneCommande> lLC = this.servCommande.getLignesByCommande(commande);
        model.addAttribute("commande", commande);
        model.addAttribute("lignes", lLC);
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("prodInfo", this.servProduit.getProductDTOs());
        model.addAttribute("cats", this.servProduit.getAllCats());
        model.addAttribute("credit", commande.getMontantHT()-total);
        model.addAttribute("clients", this.servClient.getAllClients());
        return "gest/commandDetails";
    }

    @GetMapping("/pass-commande/{id}")
    public String passCommande(Model model, @PathVariable("id") Long id, RedirectAttributes attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        List<PanierItems> lPI = this.servPanier.getPanierItemsByAdm(userId);
        Client c = this.servClient.getClientById(id);
        if(lPI.isEmpty()) {
            attributes.addFlashAttribute("error", "Aucun produit n'existe dans le panier");
            return "redirect:/gharnata/list-clients";
        }
        Commande commande = this.servCommande.saveCommand(c);
        if(commande == null) {
            attributes.addFlashAttribute("error", "une erreur s'est produite");
            return "redirect:/gharnata/list-clients";
        }
        int t = this.servCommande.saveLCom(commande, lPI);
        if(t <= 0) {
            attributes.addFlashAttribute("error", "une erreur s'est produite");
            return "redirect:/gharnata/list-clients";
        }
        model.addAttribute("mht", this.servPanier.calculPrix(lPI));
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        if(model.getAttribute("cls")==null){
            model.addAttribute("cls", new Client());
        }
        this.servPanier.deleteItemsByUserId(userId);
        return "redirect:/gharnata/command-details/"+commande.getId();
    }
    @GetMapping("/bind-c-c/{id}")
    public String bindC(@PathVariable("id") Long id, RedirectAttributes attributes) {
        Client client = this.servClient.getClientById(id);
        attributes.addFlashAttribute("cls", client);
        return "redirect:/gharnata/pass-commande";
    }
    @PostMapping("/pass-commande")
    public String passCommande(@ModelAttribute Client client, Model model, RedirectAttributes attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        Client c = this.servClient.checkClientExis(client);
        List<PanierItems> lPI = this.servPanier.getPanierItemsByAdm(userId);
        if (lPI.isEmpty()) {
            attributes.addFlashAttribute("error", "Aucun produit n'existe dans le panier");
            return "redirect:/gharnata/pass-commande";
        }
        Commande commande = this.servCommande.saveCommand(c);
        if (commande == null) {
            attributes.addFlashAttribute("error", "une erreur s'est produite");
            return "redirect:/gharnata/pass-commande";
        }
        int t = this.servCommande.saveLCom(commande, lPI);
        if (t <= 0) {
            attributes.addFlashAttribute("error", "une erreur s'est produite");
            return "redirect:/gharnata/pass-commande";
        }
        this.servPanier.deleteItemsByUserId(userId);
        return "redirect:/gharnata/show-commands";
    }
    @GetMapping("/delete-ligne-commande/{id}")
    public String deleteLigneCommande(@PathVariable("id") Long id){
        return "redirect:/gharnata/command-details/"+this.servCommande.deleteLigneComand(id);
    }
    @GetMapping("/supp-comand/{id}")
    public String suppCom(@PathVariable("id") long id ){
        this.servCommande.deleteComand(id);
        return "redirect:/gharnata/show-commands";
    }
    @PostMapping("/add-prd-to-command")
    public String addPToCmd(@RequestParam String ref, @RequestParam long id, @RequestParam int qte, RedirectAttributes attributes){
        if(qte<=0){
            attributes.addFlashAttribute("error", "Veuillez choisir une quantité.");
            return "redirect:/gharnata/command-details/"+id;
        }
        Produit p =this.servProduit.getProductByRef(ref);
        Commande c =this.servCommande.findCommandById(id);

        int qteDispo = this.servCommande.checkProductInStock(p);
        System.out.println("la quantité disponible est : " + qteDispo);
        if (qteDispo==0){
            attributes.addFlashAttribute("error", "Le produit " + p.getRef() + " est actuellement en rupture de stock.");
            attributes.addFlashAttribute("qteEnHold",0);
            return "redirect:/gharnata/command-details/"+id;
        }
        if (qte>qteDispo){
            int qteEnHold = p.getQuantite()-qteDispo;
            List<Commande> lcP = this.servCommande.findCommandProduitEx(p);
            attributes.addFlashAttribute("error", "Il existe juste "+qteDispo+" Pièce(s) de cette référence : "+p.getRef()+ " en stock.");
            attributes.addFlashAttribute("qteEnHold",qteEnHold);
            attributes.addFlashAttribute("lcP", lcP);
            return "redirect:/gharnata/command-details/"+id;
        }
        this.servCommande.addOneLCom(c, p,qte);
        return "redirect:/gharnata/command-details/"+id;
    }
    @GetMapping("/confirme-cmd/{p}/{id}")
    public String valideCmd(@PathVariable("id") long id, @PathVariable("p") int p){
        Commande c = this.servCommande.findCommandById(id);
        List<LigneCommande> lLC = this.servCommande.getLignesByCommande(c);
        this.servProduit.confirmeCmd(c,lLC);
        if(p==0){
            return "redirect:/gharnata/show-commands";
        }
        return "redirect:/gharnata/command-details/"+id;
    }


}
