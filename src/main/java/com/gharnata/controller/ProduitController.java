package com.gharnata.controller;

import com.gharnata.entity.Categorie;
import com.gharnata.entity.PanierItems;
import com.gharnata.entity.Produit;
import com.gharnata.entity.dto.ProductDTO;
import com.gharnata.service.ServImage;
import com.gharnata.service.ServNotification;
import com.gharnata.service.ServPanier;
import com.gharnata.service.ServProduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/gharnata")
public class ProduitController {
    @Autowired
    private ServProduit servProduit;
    @Autowired
    private ServImage servImage;
    @Autowired
    private ServNotification servNotification;
    @Autowired
    private ServPanier servPanier;
    @GetMapping("/add-product")
    public String addproduit(Model model){
        model.addAttribute("cats", this.servProduit.getAllCats());
        model.addAttribute("ancP", new Produit());
        model.addAttribute("notif",servNotification.nbNotif());
        return "gest/addProduct";
    }
    /*@GetMapping("/products/check-ref")
    public String checkRef(@RequestParam String ref, Model model) {
        Produit produit = servProduit.getProductByRef(ref);
        boolean existe = produit != null;
        model.addAttribute("existe", existe);
        if ( produit != null){
            model.addAttribute("produit", produit.getName());
        }

        return "fragments/ref-feedback :: feedback";
    }*/
    /*@GetMapping("/products/check-ref")
    public String checkRef(@RequestParam String ref, @RequestParam(required = false) Long excludeId, Model model) {

        Produit p = servProduit.getProductByRef(ref);
        boolean existe = (p != null) && (!p.getId().equals(excludeId));
        model.addAttribute("existe", existe);
        if ( p != null){
            model.addAttribute("produit", p.getName());
        }
        return "fragments/ref-feedback :: feedback";
    }*/
    @GetMapping("/products/check-ref")
    public String checkRef(@RequestParam String ref, @RequestParam(required = false) Long excludeId, Model model) {
        Produit p = servProduit.getProductByRef(ref);
        boolean existe = (p != null) && (excludeId == null || !p.getId().equals(excludeId));
        model.addAttribute("existe", existe);
        model.addAttribute("refVide", ref == null || ref.isBlank());
        model.addAttribute("produit", existe ? p.getName() : null);
        return "fragments/ref-feedback :: feedback";
    }
    //for modification page
    @PostMapping("/add-product")
    public String addproduit(@ModelAttribute Produit produit,int idCat, Model model, RedirectAttributes attributes, MultipartFile imagee) throws Exception {
        produit.setCategorie(this.servProduit.getCatById(idCat));
        Produit p = this.servProduit.getProductByRef(produit.getRef());
        model.addAttribute("notif",servNotification.nbNotif());
        if(p!= null){
            model.addAttribute("error", "Le produit avec cette référence existe déjà.");
            model.addAttribute("ancP",produit);
            model.addAttribute("cats", this.servProduit.getAllCats());
            return "gest/addProduct";
        }
        produit.setPic(this.servImage.treatPic(imagee));
        //produit.setPic(this.servImage.treatPic(imagee));
        if (this.servProduit.addProduit(produit) == null){
            model.addAttribute("error", "Erreur d'ajout du produit");
            model.addAttribute("cats", this.servProduit.getAllCats());
            return "gest/addProduct";
        }
        attributes.addFlashAttribute("success", "Produit ajouté avec succès.");
        return "redirect:/gharnata/add-product";
    }

    @GetMapping("/show-products")
    public String showProducts(Model model, @RequestParam(value = "ref", required = false) String ref){
        List<Produit> lP = new ArrayList<>();
        List<String> lImages= new ArrayList<>();
        if (ref != null) {
            Produit produit = servProduit.getProductByRef(ref);
            lP.add(produit);
            lImages.add(Base64.getEncoder().encodeToString(produit.getPic().getData()));
        }else{
            lP = this.servProduit.getProductsLimit();
            for (Produit produit : lP) {
                lImages.add(Base64.getEncoder().encodeToString(produit.getPic().getData()));
            }
        }
        model.addAttribute("products", lP);
        model.addAttribute("imageDatas", lImages);
        model.addAttribute("prods",this.servProduit.getProductDTOs());
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/showProducts";
    }

    @PostMapping("/show-products")
    public String showProducts(@RequestParam("ref") String ref, RedirectAttributes attributes){
        attributes.addAttribute("ref", ref);
        return "redirect:/gharnata/show-products";
    }

    @GetMapping("/show-products-stock")
    public String showProdSt(Model model, @RequestParam(value = "ref", required = false) String ref){
        List<Produit> lP = new ArrayList<>();
        if (ref != null) {
            Produit produit = servProduit.getProductByRef(ref);
            lP.add(produit);
        }else{
            lP = this.servProduit.getProducts();
        }
        List<Integer> indexs =new ArrayList<>();
        for (int i=1; i<=lP.size();i++){
            indexs.add(i);
        }
        model.addAttribute("products", lP);
        model.addAttribute("prods",this.servProduit.getProducts());
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        model.addAttribute("idxs", indexs);
        return "gest/showProdStock";
    }
    @PostMapping("/show-products-stock")
    public String showProdSt(@RequestParam("ref") String ref, RedirectAttributes attributes){
        attributes.addAttribute("ref", ref);
        return "redirect:/gharnata/show-products-stock";
    }

    @GetMapping("/product-details/{id}")
    public String showOneProduct(@PathVariable Long id, Model model){
        Produit p = this.servProduit.getProductById(id);
        model.addAttribute("product", p);
        model.addAttribute("imageData", Base64.getEncoder().encodeToString(p.getPic().getData()));
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/productDetails";
    }
    @GetMapping("/api/products/search")
    @ResponseBody
    public List<ProductDTO> searchProducts(
            @RequestParam("q") String query) {

        return servProduit.searchProducts(query);
    }
    @GetMapping("/modify-product")
    public String rechercherProduit(@RequestParam(required = false) Long productId, Model model) {
        model.addAttribute("notif", servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        // Tu n'en as plus besoin pour le datalist
        // mais tu peux le garder si cette liste est utilisée ailleurs
        model.addAttribute("prods", this.servProduit.getProductDTOs());
        if (productId != null) {
            Produit produit = this.servProduit.getProductById(productId);
            if (produit == null) {
                model.addAttribute("error", "Ce produit n'existe pas.");
                model.addAttribute("product", new Produit());
            } else {
                model.addAttribute("product", produit);
            }
        } else {
            model.addAttribute("product", new Produit());
        }
        return "gest/modifyProduct";
    }
    /*@GetMapping("/modify-product")
    public String rechercherProduit(@RequestParam(required = false) String ref, Model model) {
        model.addAttribute("notif", servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        model.addAttribute("prods", this.servProduit.getProductDTOs());
        if (ref != null && !ref.isBlank()) {
            Produit produit = this.servProduit.getProductByRef(ref);
            if (produit == null) {
                model.addAttribute("error", "Ce produit avec cette référence n'existe pas!");
            } else {
                model.addAttribute("product", produit);
            }
        }else {
            model.addAttribute("product", new Produit());
        }
        return "gest/modifyProduct";
    }*/

    @PostMapping("/modify-product/{id}")
    public String modifierProduit(@PathVariable long id,
                                  @RequestParam String ref,
                                  @RequestParam int idCat,
                                  @RequestParam String name,
                                  @RequestParam double prixVente,
                                  @RequestParam(defaultValue = "0") int quantite,
                                  @RequestParam MultipartFile imagee,
                                  RedirectAttributes attributes) {

        if (prixVente < 0 || quantite < 0) {
            attributes.addFlashAttribute("error", "Le prix et la quantité ne peuvent pas être négatifs.");
            return "redirect:/gharnata/modify-product?ref=" + ref;
        }

        Produit p = this.servProduit.getProductById(id);
        if (p == null) {
            attributes.addFlashAttribute("error", "Produit introuvable.");
            return "redirect:/gharnata/modify-product";
        }

        Categorie cat = this.servProduit.getCatById(idCat);
        if (cat == null) {
            attributes.addFlashAttribute("error", "Catégorie invalide.");
            return "redirect:/gharnata/modify-product?ref=" + p.getRef();
        }

        p.setQuantite(p.getQuantite() + quantite);
        p.setName(name);
        p.setRef(ref);
        p.setPrixVente(prixVente);
        p.setCategorie(cat);
        Produit pt = this.servProduit.addProduit(p);

        if (pt == null) {
            attributes.addFlashAttribute("error", "Un problème est survenu lors de la modification.");
            return "redirect:/gharnata/modify-product";
        }

        if (!imagee.isEmpty()) {
            if (pt.getPic() != null) {
                this.servImage.modifyPic(imagee, pt.getPic().getId());
            } else {
                try {
                    pt.setPic(this.servImage.treatPic(imagee));
                } catch (Exception e) {
                    attributes.addFlashAttribute("error", "Un problème est survenu lors de la modification de l'image.");
                    return "redirect:/gharnata/modify-product";
                }
                this.servProduit.addProduit(pt);
            }
        }

        attributes.addFlashAttribute("success", "Produit bien modifié");
        if (pt.getQuantite() > 5) {
            this.servNotification.deletEnr(pt);
        }
        return "redirect:/gharnata/modify-product";
    }
    //working function
    /*@PostMapping("/modify-product")
    public String modifyProd(@RequestParam("ref") String ref, int idCat, Model model, RedirectAttributes attributes, long id, String name, double prixVente, int quantite, MultipartFile imagee) throws Exception {
        System.out.println("here1");
        if (id != 0){
            System.out.println("here2");
            Produit p = this.servProduit.getProductById(id);
            System.out.println("here3");
            if (p == null){
                System.out.println("here4");
                attributes.addFlashAttribute("error", "Produit introuvable.");
                return "redirect:/gharnata/modify-product";
            }
            p.setQuantite(p.getQuantite()+quantite);
            p.setName(name);
            p.setRef(ref);
            p.setPrixVente(prixVente);
            p.setCategorie(this.servProduit.getCatById(idCat));
            Produit pt = this.servProduit.addProduit(p);
            System.out.println("here5");
            if (pt == null){
                System.out.println("here6");
                attributes.addFlashAttribute("error", "Un problème est survenu lors de la modification.");
                return "redirect:/gharnata/modify-product";
            }
            System.out.println("here7");
            if (!imagee.isEmpty()) {
                System.out.println("here8");
                if (pt.getPic() != null) {
                    System.out.println("here9");
                    this.servImage.modifyPic(imagee, pt.getPic().getId());
                } else {
                    System.out.println("here10");
                    pt.setPic(this.servImage.treatPic(imagee));
                    System.out.println("here11");
                    this.servProduit.addProduit(pt);
                }
            }
            System.out.println("here12");
            attributes.addFlashAttribute("success", "Produit bien modifié");
            if(pt.getQuantite() > 5){
                System.out.println("here13");
                this.servNotification.deletEnr(pt);
            }
            return "redirect:/gharnata/modify-product";
        }
        System.out.println("here14");
        Produit produit = this.servProduit.getProductByRef(ref);
        System.out.println("here15");
        if(produit == null){
            System.out.println("here16");
            attributes.addFlashAttribute("error", "Ce produit avec cette référence n'existe pas!");
            return "redirect:/gharnata/modify-product";
        }
        System.out.println("here17");
        model.addAttribute("prods",this.servProduit.getProductDTOs());
        model.addAttribute("product", produit);
        model.addAttribute("notif", servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        System.out.println("here18");
        return "gest/modifyProduct";
    }*/
    /*@PostMapping("/modify-product")
    public String modifyProd(@RequestParam("ref") String ref, int idCat, Model model, RedirectAttributes attributes, long id, String name, double prixVente, int quantite, MultipartFile imagee ){
        if (id != 0){
            Produit p = this.servProduit.getProductById(id);
            p.setQuantite(p.getQuantite()+quantite);
            p.setName(name);
            p.setRef(ref);
            p.setPrixVente(prixVente);
            p.setCategorie(this.servProduit.getCatById(idCat));
            Produit pt = this.servProduit.addProduit(p);
            this.servImage.modifyPic(imagee, pt.getPic().getId());
            if(pt.getName()!=null){
                attributes.addFlashAttribute("success", "Produit bien modifié");
                if(pt.getQuantite()> 5){
                    this.servNotification.deletEnr(pt);
                }
            }else{
                attributes.addFlashAttribute("error", "Un problème est survenu lors de la modification.");
            }
            return "redirect:/gharnata/modify-product";
        }
        Produit produit = this.servProduit.getProductByRef(ref);
        if(produit == null){
             attributes.addFlashAttribute("error", "Ce produit avec cette référence n'existe pas!");
             return "redirect:/gharnata/modify-product";
        }
        model.addAttribute("prods",this.servProduit.getProducts());
        model.addAttribute("product", produit);
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/modifyProduct";
    }*/
    @GetMapping("/delete-product/{page}/{id}")
    public String deletProduct(@PathVariable Long id, RedirectAttributes attributes, @PathVariable String page){
        Produit produit = this.servProduit.getProductById(id);
        this.servPanier.checkProductExi(produit);
        this.servNotification.checkProductExi(produit);
        this.servProduit.deletePrById(id);
        if(page.equals("list")){
            return "redirect:/gharnata/show-products-stock";
        }
        return "redirect:/gharnata/show-products";
    }
    @PostMapping("/delete-products")
    public String supprimerProduits(@RequestParam("prod_ids") List<Long> ids) {
        List<Produit> lp = this.servProduit.getAllByIds(ids);
        this.servPanier.checkProductsExi(lp);
        this.servNotification.checkProductsExi(lp);
        servProduit.deleteAllById(ids);
        return "redirect:/gharnata/show-products-stock";
    }
    @GetMapping("/pass-mass")
    public String passMass(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Récupérez l'ID de l'utilisateur
        String userId = authentication.getName();
        List<PanierItems> panierItems = this.servPanier.getPanierItemsByAdm(userId);
        List<ProductDTO> lPI = this.servProduit.getProductDTOs();
        List<Integer> indexs =new ArrayList<>();
        for (int i=1; i<=panierItems.size();i++){
            indexs.add(i);
        }
        model.addAttribute("prodInfos", lPI);
        model.addAttribute("items",panierItems);
        model.addAttribute("mht", this.servPanier.calculPanierMss(panierItems));
        model.addAttribute("idxs", indexs);
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/passMasse";
    }
    /*@PostMapping("/add-categorie")
    public String addCat(@RequestParam String cat, @RequestParam(defaultValue = "/gharnata/add-product") String redirectTo){
        this.servProduit.addCat(cat);
        return "redirect:" + redirectTo;
    }*/
    @PostMapping("/add-categorie")
    public String addCat(@RequestParam String cat,
                         @RequestParam(defaultValue = "/gharnata/add-product") String redirectTo,
                         @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                         Model model) {
        this.servProduit.addCat(cat);
        if ("true".equals(hxRequest)) {
            model.addAttribute("cats", this.servProduit.getAllCats());
            return "fragments/cat-select :: catSelect";
        }
        return "redirect:" + redirectTo;
    }


}
