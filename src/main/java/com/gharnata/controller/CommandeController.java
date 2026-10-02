package com.gharnata.controller;
import com.gharnata.entity.*;
import com.gharnata.service.*;
import jakarta.servlet.http.HttpServletResponse;
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
    @PostMapping("/payment")
    public String payment(@RequestParam("montant") double montant, @RequestParam("idC") long idC, RedirectAttributes attributes){
        Client client = this.servClient.getClientById(idC);
        Compte compte = this.servClient.getCmptById(client.getCompte().getId());
        if (montant> client.getCompte().getCredit()){
            attributes.addFlashAttribute("error", "Le montant que vous avez introduit est supérieur à celui du crédit.");
            return "redirect:/gharnata/client-details/"+client.getId();
        }
        if (this.servCommande.savePaym(client,montant,"Paiement d'un montant de : "+ montant)!=null){
            compte.setCredit(compte.getCredit()-montant);
            this.servClient.saveCmpt(compte);
        }
        return "redirect:/gharnata/client-details/"+client.getId();
    }

    /*@PostMapping("/pay-cmd")
    public void paycmd (@RequestParam double mt, @RequestParam("id") long idcmd, @RequestParam("client_id") Long client_id, HttpServletResponse response, @RequestParam(value = "tmp", required = false) String tmp, @RequestParam("pInfo") String modpai){
        //a modifier
        Commande commande = this.servCommande.findCommandById(idcmd);
        double total= this.servPaie.calculateTotalPaiement(commande);
        if(total== commande.getMontantHT()){
            System.out.println("commande deja payée");
            return;
        }else {
            List<Paiement> listPai = this.servPaie.getPaiementsByCommande(commande);
            if (listPai.isEmpty() && mt >= commande.getMontantTTC()){
                if(mt == commande.getMontantHT()){
                    Client client = this.servClient.getClientById(client_id);
                    Compte compte = this.servClient.getCmptById(commande.getClient().getCompte().getId());
                    Paiement paiement = this.servPaie.effectuerPaiement(commande, mt, modpai, "Paiement TOTAL de la commande n " +commande.getId() + ", par << "+ client.getSte()+ " >>");
                    Facture facture = this.servFacture.creeFacture(commande, paiement, client);
                    System.out.println("date: "+ facture.getDate());
                    paiement.setFacture(facture);
                    this.servPaie.savePaie(paiement);
                    List<LigneCommande> lcmd = this.servCommande.getLignesByCommande(commande);
                    for (LigneCommande lc : lcmd){
                        LigneFacture lf = new LigneFacture();
                        lf.setProductId(lc.getProductId());
                        lf.setProductName(lc.getProductName());
                        lf.setRef(lc.getRef());
                        lf.setPrixVente(lc.getPrixVente());
                        lf.setQuantity(lc.getQuantity());
                        lf.setMontant(lc.getMontant());
                        lf.setFacture(facture);
                        this.servFacture.saveLF(lf);
                    }
                    facture.setMontantHT(commande.getMontantHT());
                    facture.setMTVA(commande.getMTVA());
                    facture.setMontantTTC(commande.getMontantTTC());
                    int dirhams = (int) facture.getMontantTTC();
                    int centimes = (int) Math.round((facture.getMontantTTC() - dirhams) * 100);
                    String mtl = NombreEnLettres.convertir(dirhams) + " dirhams";
                    if (centimes > 0) {
                        mtl += " et " + NombreEnLettres.convertir(centimes) + " centimes";
                    }
                    facture.setMtl(mtl);
                    facture = this.servFacture.saveFac(facture);
                    compte.setCredit(compte.getCredit()- mt);
                    this.servClient.saveCmpt(compte);
                    commande.setPaye(true);
                    this.servCommande.saveCommand(commande);
                    this.servFacture.genererFacture(facture,tmp, response);
                    System.out.println("paiement effectué");
                }else if(mt > commande.getMontantHT()){
                    System.out.println("montant introduit est supperieur du montant du commande");
                    return;
                }
            }else if(mt < commande.getMontantTTC()){
                if (mt + total <= commande.getMontantTTC()){
                    Client client = this.servClient.getClientById(client_id);
                    Compte compte = this.servClient.getCmptById(commande.getClient().getCompte().getId());
                    Paiement paiement = this.servPaie.effectuerPaiement(commande, mt, modpai, "Paiement PARTIEL de la commande n " +commande.getId() + ", par << "+ client.getSte()+ " >>");
                    Facture facture = this.servFacture.creeFacture(commande, paiement, client);
                    paiement.setFacture(facture);
                    this.servPaie.savePaie(paiement);
                    facture.setMontantTTC(mt);
                    facture.setMontantHT(facture.getMontantTTC()/1.2);
                    facture.setMTVA(facture.getMontantTTC()-facture.getMontantHT());
                    facture = this.servFacture.saveFac(facture);
                    compte.setCredit(compte.getCredit()- mt);
                    this.servClient.saveCmpt(compte);
                    if(mt+total == commande.getMontantTTC()){
                        commande.setPaye(true);
                        this.servCommande.saveCommand(commande);
                    }
                    this.servCommande.persFactItems(facture, commande);
                    this.servFacture.genererFacture(facture,tmp, response);
                    System.out.println("paiement effectué");
                }else {
                    System.out.println("montant introduit est supperieur du montant restant a payer");
                    return;
                }

            }
        }
        return;
    }*/
    @PostMapping("/pay-cmd")
    public String paycmd (@RequestParam double mt, @RequestParam("id") long idcmd, @RequestParam("client_id") Long client_id, HttpServletResponse response, @RequestParam(value = "tmp", required = false) String tmp, @RequestParam("pInfo") String modpai, RedirectAttributes attributes) throws Exception {
        //a modifier
        Commande commande = this.servCommande.findCommandById(idcmd);
        double total= this.servPaie.calculateTotalPaiement(commande);
        if(total== commande.getMontantHT()){
            System.out.println("commande déja payée");
            attributes.addFlashAttribute("message", "commande deja payée");
            return "redirect:/gharnata/command-details/"+ commande.getId();
        }else {
            List<Paiement> listPai = this.servPaie.getPaiementsByCommande(commande);
            if (mt + total > commande.getMontantHT()){
                System.out.println("montant introduit est supperieur du montant a regler");
                attributes.addFlashAttribute("message", "Le montant introduit est supérieur au montant à régler.");
                return "redirect:/gharnata/command-details/"+ commande.getId();
            }else{
                Client client = this.servClient.getClientById(client_id);
                Compte compte = this.servClient.getCmptById(commande.getClient().getCompte().getId());
                Paiement paiement = new Paiement();
                if (listPai.isEmpty() && mt == commande.getMontantHT()){
                    paiement = this.servPaie.effectuerPaiement(commande, mt, modpai, "Paiement TOTAL de la commande n°" +commande.getId() + ", par  |"+ client.getSte()+ "|");
                    commande.setPaye(true);
                }else if(mt + total <= commande.getMontantHT()){
                    paiement = this.servPaie.effectuerPaiement(commande, mt, modpai, "Paiement PARTIEL de la commande n°" +commande.getId() + ", par  |"+ client.getSte()+ "|");
                    if(mt+total == commande.getMontantHT()){
                        commande.setPaye(true);
                    }
                }
                this.servCommande.saveCommand(commande);
                Facture facture = this.servFacture.creeFacture(commande, paiement, client);
                facture.setMontantTTC(mt);
                facture.setMontantHT(facture.getMontantTTC()/1.2);
                facture.setmTVA(facture.getMontantTTC()-facture.getMontantHT());
                facture = this.servFacture.saveFac(facture);
                paiement.setFacture(facture);
                this.servPaie.savePaie(paiement);
                compte.setCredit(compte.getCredit()- mt);
                this.servClient.saveCmpt(compte);
                this.servCommande.persFactItems(facture, commande);
                this.servFacture.genererFacture(facture,tmp, response);

            }
        }
        return null;
    }

}
