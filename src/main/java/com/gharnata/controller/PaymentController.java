package com.gharnata.controller;

import com.gharnata.entity.*;
import com.gharnata.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/gharnata")

public class PaymentController {

    private final ServClient servClient;
    private final ServPaiement servPaiement;
    private final ServNotification servNotification;
    private final ServProduit servProduit;
    private final ServCommande servCommande;
    private final ServFacture servFacture;

    public PaymentController(ServClient servClient, ServPaiement servPaiement,
                             ServNotification servNotification, ServProduit servProduit,
                             ServCommande servCommande, ServFacture servFacture) {
        this.servClient = servClient;
        this.servPaiement = servPaiement;
        this.servNotification = servNotification;
        this.servProduit = servProduit;
        this.servCommande = servCommande;
        this.servFacture = servFacture;
    }

    @GetMapping("/paiements/{id}")
    public String showPaie(Model model, @PathVariable Long id){
        Client client = this.servClient.getClientById(id);
        List<Paiement> lP = this.servPaiement.getPaymentsByClient(client);
        model.addAttribute("client", client);
        model.addAttribute("payments", lP);
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/showPayments";
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
    @PostMapping("/pay-cmd")
    public String paycmd (@RequestParam double mt, @RequestParam("id") long idcmd,
                          @RequestParam("client_id") Long client_id, HttpServletResponse response,
                          @RequestParam(value = "tmp", required = false) String tmp,
                          @RequestParam("pInfo") String modpai, RedirectAttributes attributes) throws Exception {
        //a modifier
        Commande commande = this.servCommande.findCommandById(idcmd);
        double total= this.servPaiement.calculateTotalPaiement(commande);
        if(total== commande.getMontantHT()){
            System.out.println("commande déja payée");
            attributes.addFlashAttribute("message", "commande deja payée");
            return "redirect:/gharnata/command-details/"+ commande.getId();
        }else {
            List<Paiement> listPai = this.servPaiement.getPaiementsByCommande(commande);
            if (mt + total > commande.getMontantHT()){
                System.out.println("montant introduit est supperieur du montant a regler");
                attributes.addFlashAttribute("message", "Le montant introduit est supérieur au montant à régler.");
                return "redirect:/gharnata/command-details/"+ commande.getId();
            }else{
                Client client = this.servClient.getClientById(client_id);
                Compte compte = this.servClient.getCmptById(commande.getClient().getCompte().getId());
                Paiement paiement = new Paiement();
                if (listPai.isEmpty() && mt == commande.getMontantHT()){
                    paiement = this.servPaiement.effectuerPaiement(commande, mt, modpai, "Paiement TOTAL de la commande n°" +commande.getId() + ", par  |"+ client.getSte()+ "|");
                    commande.setPaye(true);
                }else if(mt + total <= commande.getMontantHT()){
                    paiement = this.servPaiement.effectuerPaiement(commande, mt, modpai, "Paiement PARTIEL de la commande n°" +commande.getId() + ", par  |"+ client.getSte()+ "|");
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
                this.servPaiement.savePaie(paiement);
                compte.setCredit(compte.getCredit()- mt);
                this.servClient.saveCmpt(compte);
                this.servCommande.persFactItems(facture, commande);
                this.servFacture.genererFacture(facture,tmp, response);

            }
        }
        return null;
    }

}
