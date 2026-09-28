package com.gharnata.service;

import com.gharnata.compenents.ResourceUtils;
import com.gharnata.entity.*;
import com.gharnata.repository.RepFacture;
import com.gharnata.repository.RepLigneFac;
import com.gharnata.service.footer.PdfFooterProdsEvent;
import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.gharnata.service.footer.BonFooter;

@Service
public class ServFacture {
    @Autowired
    private RepFacture repFacture;
    @Autowired
    private RepLigneFac repLigneFac;
    @Autowired
    private NumeroFactureService numeroFactureService;
    @Autowired
    private EmailService emailService;
    public Facture genererFacture(Facture facture, String tmp, HttpServletResponse response) throws Exception{
        List<LigneFacture> lLF = this.repLigneFac.findAllByFacture(facture);
        Document document = new Document();
        try {
            // Créer une police en gras
            Font policeGras = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            //Font pg = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            String nomF= "FACTURE"+facture.getId()+"_"+facture.getClient().getSte()+".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(nomF));
            PdfFooterEvent event = new PdfFooterEvent();
            // Ajouter un événement pour gérer le pied de page
            writer.setPageEvent(event);
            document.open();
            //Image logo = Image.getInstance("classes/static/images/lg.png");
            //Image logo = Image.getInstance("src/main/resources/static/images/lg.png");
            Image logo = Image.getInstance(ResourceUtils.load("static/images/lg.png"));

            logo.scaleToFit(500, 150); // Ajuster la taille du logo selon vos besoins
            logo.setAlignment(Element.ALIGN_LEFT);
            document.add(logo);
            //informations de l'entreprise
            String cel = "STE "+ facture.getClient().getSte()+"\n\n";
            if (facture.getClient().getIce() != null && !facture.getClient().getIce().trim().isEmpty() && !facture.getClient().getIce().toLowerCase().startsWith("cin") && !facture.getClient().getIce().toLowerCase().startsWith("ice")) {
                cel += "I.C.E : " + facture.getClient().getIce();
            }
            cel+= "\n\n";
            if(!Objects.equals(facture.getClient().getAdresse(), "")){
                cel+= facture.getClient().getAdresse();
            }
            PdfPCell cellule = new PdfPCell(new Phrase(cel ,policeGras));
            //PdfPCell cellule = new PdfPCell(new Phrase("STE "+ facture.getClient().getSte()+"\n\nI.C.E : " + facture.getClient().getIce()+"\n\n"+facture.getClient().getAdresse(),policeGras));
            cellule.setBorder(Rectangle.BOX);
            cellule.setBorderColor(BaseColor.BLACK);
            cellule.setBorderWidth(1f);
            cellule.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellule.setPaddingTop(7f);
            cellule.setPaddingBottom(10f);
            PdfPTable tt = new PdfPTable(1);
            tt.setWidthPercentage(50);
            tt.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tt.addCell(cellule);
            document.add(tt);
            //informations de la facture
            PdfPTable tab = new PdfPTable(3); // 5 colonnes
            tab.setWidthPercentage(100);
            tab.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tab.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tab.setSpacingBefore(20f);// Ajouter un espacement avant la table
            tab.setSpacingAfter(20f);
            tab.addCell(new Phrase("Facture nº : "+ facture.getId(),policeGras));
            tab.addCell(new Phrase("Date : "+ facture.getDate(),policeGras));
            tab.addCell(new Phrase("Code Client : "+ facture.getClient().getId(), policeGras));
            document.add(tab);
            // Ajouter le tableau des détails de la facture
            PdfPTable table = new PdfPTable(5); // 5 colonnes
            table.setWidthPercentage(100);
            table.getDefaultCell().setPaddingTop(2f);
            table.getDefaultCell().setPaddingBottom(5f);
            table.setWidths(new float[]{20,45,10, 10, 15});
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            // Ajouter une cellule pour chaque détail de la facture
            table.addCell(createHeaderCell("Code Article", policeGras));
            table.addCell(createHeaderCell("Désignation", policeGras));
            table.addCell(createHeaderCell("Qté", policeGras));
            table.addCell(createHeaderCell("P.U", policeGras));
            table.addCell(createHeaderCell("P.T", policeGras));

            // Ajouter des lignes de données (à remplacer par vos propres données)
            for (LigneFacture lf : lLF){
                table.addCell(lf.getRef());
                table.addCell(lf.getProductName());
                table.addCell(String.valueOf(lf.getQuantity()));
                table.addCell(String.format("%.2f",lf.getPrixVente()));
                table.addCell(String.format("%.2f", lf.getMontant()));
            }
            // Ajouter le tableau au document
            document.add(table);
            //les montants
            PdfPTable tm = new PdfPTable(3); // 5 colonnes
            tm.setWidthPercentage(100);
            tm.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tm.getDefaultCell().setPaddingTop(5f);
            tm.getDefaultCell().setPaddingBottom(5f);
            tm.setSpacingBefore(20f);
            tm.setSpacingAfter(20f);
            //header
            tm.addCell(createHeaderCell("Montant total Hors taxes (HT)", policeGras));
            tm.addCell(createHeaderCell("Montant TVA (20%)", policeGras));
            tm.addCell(createHeaderCell("Montant TTC", policeGras));
            //body
            tm.addCell(String.format("%.2f",facture.getMontantHT())+" DH");
            tm.addCell(String.format("%.2f",facture.getmTVA())+" DH");
            tm.addCell(new Phrase(String.format("%.2f",facture.getMontantTTC())+" DH", policeGras));
            document.add(tm);
            //mode de paie
            PdfPTable mp = new PdfPTable(2);
            mp.setHorizontalAlignment(Element.ALIGN_LEFT);
            mp.setWidthPercentage(100);
            mp.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            mp.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            mp.setSpacingAfter(10f);
            mp.setWidths(new float[]{22,78});
            //heaed
            mp.addCell(new Phrase("Mode de Paiment : ", policeGras));
            mp.addCell(new Phrase(facture.getPaiement().getModPai()));
            document.add(mp);
            //mtl
            PdfPTable mtl = new PdfPTable(2);
            mtl.setHorizontalAlignment(Element.ALIGN_LEFT);
            mtl.setWidthPercentage(100);
            mtl.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            mtl.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            mtl.setSpacingAfter(20f);
            mtl.setWidths(new float[]{46,54});
            //head
            mtl.addCell(new Phrase("Arrêté la présente facture à la somme de: ", policeGras));
            mtl.addCell(new Phrase(facture.getMtl()));
            document.add(mtl);

            /*if ("on".equals(tmp)) {
                try {
                    Image cachet = Image.getInstance("src/main/resources/static/images/cachet.png");
                    cachet.scaleToFit(120, 120); // Redimensionne l’image

                    // Position en bas à droite (ajuste X et Y selon ta page)
                    float x = 420f; // axe horizontal (gauche → droite)
                    float y = 100f; // axe vertical (bas → haut)

                    cachet.setAbsolutePosition(x, y); // Position absolue
                    PdfContentByte canvas = writer.getDirectContentUnder();
                    canvas.addImage(cachet); // Ajout sous le texte (comme un tampon)
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
            if ("on".equals(tmp)) {
                try {
                    //Image cachet = Image.getInstance("classes/static/images/cachet.png");
                    //Image cachet = Image.getInstance("src/main/resources/static/images/cachet.png");
                    Image cachet = Image.getInstance(ResourceUtils.load("static/images/cachet.png"));
                    cachet.scaleToFit(150, 150); // Redimensionner selon besoin
                    cachet.setAbsolutePosition(420f, 50f); // Position sur la page

                    // Créer un état graphique avec opacité
                    PdfGState gstate = new PdfGState();
                    gstate.setFillOpacity(0.9f); // 0.0 = transparent, 1.0 = opaque

                    PdfContentByte canvas = writer.getDirectContent(); // au-dessus du contenu
                    canvas.saveState();
                    canvas.setGState(gstate);
                    canvas.addImage(cachet);
                    canvas.restoreState(); // très important pour ne pas affecter les autres éléments
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            document.close();
            // Récupérer le fichier généré
            File pdfFile = new File(nomF);
            /*emailService.sendEmailWithAttachment(
                    "elghouzliyassine01@gmail.com",
                    "Une facture a été générée",
                    "Bonjour, veuillez trouver ci-joint votre facture.",
                    pdfFile
            );*/
            // Définir les entêtes pour le téléchargement du fichier
            response.setContentType("application/pdf");
            String headerKey ="Content-Disposition";
            String headerValue = "attachment; filename="+ nomF;
            response.setHeader(headerKey, headerValue);

            // Copier le contenu du fichier PDF dans la réponse HTTP
            response.getOutputStream().write(org.apache.commons.io.IOUtils.toByteArray(new java.io.FileInputStream(nomF)));
            response.getOutputStream().flush();
            response.getOutputStream().close();

            // Supprimer le fichier local après l'exportation
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(nomF));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } /*catch (MessagingException e) {
            throw new RuntimeException(e);
        }*/
        return null;
    }

    public void genererBon(Commande commande, List<LigneCommande> lLC, HttpServletResponse response, String rap) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Document document = new Document();
        try {
            // Créer une police en gras
            Font policeGras = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font pg = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            String nomF= "BON"+commande.getId()+"_"+commande.getClient().getSte()+".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(nomF));
            BonFooter event = new BonFooter();
            // Ajouter un événement pour gérer le pied de page
            writer.setPageEvent(event);
            document.open();
            //Image logo = Image.getInstance("classes/static/assets/images/logo/lgb.png");
            //Image logo = Image.getInstance("src/main/resources/static/images/lgb.png");
            Image logo = Image.getInstance(ResourceUtils.load("static/images/lgb.png"));
            logo.scaleToFit(200, 150); // Ajuster la taille du logo selon vos besoins
            logo.setAlignment(Element.ALIGN_LEFT);
            document.add(logo);
            //informations de l'entreprise
            float distanceHautBas = 10f;
            PdfPCell cellule = new PdfPCell(new Phrase(commande.getClient().getSte()+"\n\n"+commande.getClient().getAdresse(),policeGras));
            cellule.setBorder(Rectangle.BOX);
            cellule.setBorderColor(BaseColor.BLACK);
            cellule.setBorderWidth(1f);
            cellule.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellule.setPaddingTop(distanceHautBas);
            cellule.setPaddingBottom(distanceHautBas);
            PdfPTable tt = new PdfPTable(1);
            tt.setWidthPercentage(50);
            tt.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tt.addCell(cellule);
            document.add(tt);
            //informations de la facture
            //produits
            PdfPTable tab = new PdfPTable(3); // 5 colonnes
            tab.setWidthPercentage(100);
            tab.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tab.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tab.setSpacingBefore(20f);// Ajouter un espacement avant la table
            tab.setSpacingAfter(20f);
            tab.addCell(new Phrase("Bon de livraison nº : "+ commande.getId(),policeGras));
            tab.addCell(new Phrase("Date : "+ sdf.format(new Date()),policeGras));
            tab.addCell(new Phrase("Code Client : "+ commande.getClient().getId(), policeGras));
            document.add(tab);
            // Ajouter le tableau des détails de la facture
            PdfPTable table = new PdfPTable(5); // 5 colonnes
            PdfPCell cc = new PdfPCell(new Phrase("Quantité", pg));
            table.setWidthPercentage(100);
            table.setWidths(new float[]{20,45,10, 10, 15});
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            // Ajouter une cellule pour chaque détail de la facture
            table.addCell(new Phrase("Référence", pg));
            table.addCell(new Phrase("Désignation", pg));
            table.addCell(cc);
            table.addCell(new Phrase("Prix unitaire", pg));
            table.addCell(new Phrase("Montant HT",pg));

            // Ajouter des lignes de données (à remplacer par vos propres données)
            for (LigneCommande lc : lLC){
                table.addCell(lc.getRef());
                table.addCell(lc.getProductName());
                table.addCell(String.valueOf(lc.getQuantity()));
                table.addCell(String.format("%.2f",lc.getPrixVente()));
                table.addCell(String.format("%.2f", lc.getMontant()));
            }
            // Ajouter le tableau au document
            document.add(table);
            //les montants
            PdfPTable tm = new PdfPTable(1); // 5 colonnes
            tm.setWidthPercentage(25);
            tm.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tm.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tm.getDefaultCell().setPaddingTop(5f);
            tm.getDefaultCell().setPaddingBottom(5f);
            tm.setSpacingBefore(20f);
            tm.setSpacingAfter(20f);
            //header
            tm.addCell(new Phrase("Montant HT", policeGras));
            //body
            tm.addCell(new Phrase(String.format("%.2f",commande.getMontantHT())+" DH", policeGras));
            document.add(tm);
            Font pgg = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            if (Objects.equals(rap, "on")){
                if (commande.getClient().getCompte().getCredit()>0){
                    //mode de paie
                    PdfPTable crd = new PdfPTable(2);
                    crd.setHorizontalAlignment(Element.ALIGN_LEFT);
                    crd.setWidthPercentage(100);
                    crd.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    crd.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    crd.setSpacingAfter(10f);
                    crd.setWidths(new float[]{12,88});
                    //heaed
                    crd.addCell(new Phrase("RAPPEL : ", pgg));
                    crd.addCell(new Phrase(String.format("%.2f",commande.getClient().getCompte().getCredit())+" DHS RESTE À REGLER", pgg));
                    document.add(crd);
                }
            }
            document.close();


            // Définir les entêtes pour le téléchargement du fichier
            response.setContentType("application/pdf");
            String headerKey ="Content-Disposition";
            String headerValue = "attachment; filename="+ nomF;
            response.setHeader(headerKey, headerValue);

            // Copier le contenu du fichier PDF dans la réponse HTTP
            response.getOutputStream().write(org.apache.commons.io.IOUtils.toByteArray(new java.io.FileInputStream(nomF)));
            response.getOutputStream().flush();
            response.getOutputStream().close();

            // Supprimer le fichier local après l'exportation
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(nomF));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void simulerFacture(SimFacture simFacture, List<LigneSFacture> lSF, HttpServletResponse response){
        Document document = new Document();
        try {
            // Créer une police en gras
            Font policeGras = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font pg = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            String nomF= "FACTURE"+simFacture.getId()+"_"+simFacture.getClient().getSte()+".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(nomF));
            PdfFooterEvent event = new PdfFooterEvent();
            // Ajouter un événement pour gérer le pied de page
            writer.setPageEvent(event);
            document.open();
            //Image logo = Image.getInstance("classes/static/images/lg.png");
            //Image logo = Image.getInstance("src/main/resources/static/images/lg.png");
            Image logo = Image.getInstance(ResourceUtils.load("static/images/lg.png"));
            logo.scaleToFit(500, 150); // Ajuster la taille du logo selon vos besoins
            logo.setAlignment(Element.ALIGN_LEFT);
            document.add(logo);
            //informations de l'entreprise
            float distanceHautBas = 10f;
            PdfPCell cellule = new PdfPCell(new Phrase("STE "+ simFacture.getClient().getSte()+"\n\nI.C.E : " + simFacture.getClient().getIce()+"\n\n"+simFacture.getClient().getAdresse(),policeGras));
            cellule.setBorder(Rectangle.BOX);
            cellule.setBorderColor(BaseColor.BLACK);
            cellule.setBorderWidth(1f);
            cellule.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellule.setPaddingTop(distanceHautBas);
            cellule.setPaddingBottom(distanceHautBas);
            PdfPTable tt = new PdfPTable(1);
            tt.setWidthPercentage(50);
            tt.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tt.addCell(cellule);
            document.add(tt);
            //informations de la facture
            //produits
            PdfPTable tab = new PdfPTable(3); // 5 colonnes
            tab.setWidthPercentage(100);
            tab.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tab.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tab.setSpacingBefore(20f);// Ajouter un espacement avant la table
            tab.setSpacingAfter(20f);
            tab.addCell(new Phrase("Facture nº : "+ simFacture.getId(),policeGras));
            tab.addCell(new Phrase("Date : "+ simFacture.getDate(),policeGras));
            tab.addCell(new Phrase("Code Client : "+ simFacture.getClient().getId(), policeGras));
            document.add(tab);
            // Ajouter le tableau des détails de la facture
            PdfPTable table = new PdfPTable(5); // 5 colonnes
            PdfPCell cc = new PdfPCell(new Phrase("Quantité", pg));
            table.setWidthPercentage(100);
            table.setWidths(new float[]{20,45,10, 10, 15});
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            // Ajouter une cellule pour chaque détail de la facture
            table.addCell(new Phrase("Référence", pg));
            table.addCell(new Phrase("Désignation", pg));
            table.addCell(cc);
            table.addCell(new Phrase("Prix unitaire", pg));
            table.addCell(new Phrase("Montant HT",pg));

            // Ajouter des lignes de données (à remplacer par vos propres données)
            for (LigneSFacture lc : lSF){
                table.addCell(lc.getRef());
                table.addCell(lc.getProductName());
                table.addCell(String.valueOf(lc.getQuantity()));
                table.addCell(String.format("%.2f",lc.getPrixVente()));
                table.addCell(String.format("%.2f", lc.getMontant()));
            }
            // Ajouter le tableau au document
            document.add(table);
            //les montants
            PdfPTable tm = new PdfPTable(3); // 5 colonnes
            tm.setWidthPercentage(100);
            tm.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tm.getDefaultCell().setPaddingTop(5f);
            tm.getDefaultCell().setPaddingBottom(5f);
            tm.setSpacingBefore(20f);
            tm.setSpacingAfter(20f);
            //header
            tm.addCell(new Phrase("Montant total Hors taxes (HT)", policeGras));
            tm.addCell(new Phrase("Montant TVA (20%)", policeGras));
            tm.addCell(new Phrase("Montant TTC", policeGras));
            //body
            tm.addCell(String.format("%.2f",simFacture.getMontantHT())+" DH");
            tm.addCell(String.format("%.2f",simFacture.getmTVA())+" DH");
            tm.addCell(new Phrase(String.format("%.2f",simFacture.getMontantTTC())+" DH", policeGras));
            document.add(tm);
            //mode de paie
            PdfPTable mp = new PdfPTable(2);
            mp.setHorizontalAlignment(Element.ALIGN_LEFT);
            mp.setWidthPercentage(100);
            mp.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            mp.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            mp.setSpacingAfter(10f);
            mp.setWidths(new float[]{22,78});
            //heaed
            mp.addCell(new Phrase("Mode de Paiment : ", policeGras));
            mp.addCell(new Phrase(simFacture.getModPai()));
            document.add(mp);
            //mtl
            PdfPTable mtl = new PdfPTable(2);
            mtl.setHorizontalAlignment(Element.ALIGN_LEFT);
            mtl.setWidthPercentage(100);
            mtl.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            mtl.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            mtl.setSpacingAfter(20f);
            mtl.setWidths(new float[]{45,55});
            //head
            mtl.addCell(new Phrase("Arrêté la présente facture à la somme de : ", policeGras));
            mtl.addCell(new Phrase(simFacture.getMtl()));
            document.add(mtl);
            document.close();


            // Définir les entêtes pour le téléchargement du fichier
            response.setContentType("application/pdf");
            String headerKey ="Content-Disposition";
            String headerValue = "attachment; filename="+ nomF;
            response.setHeader(headerKey, headerValue);

            // Copier le contenu du fichier PDF dans la réponse HTTP
            response.getOutputStream().write(org.apache.commons.io.IOUtils.toByteArray(new java.io.FileInputStream(nomF)));
            response.getOutputStream().flush();
            response.getOutputStream().close();

            // Supprimer le fichier local après l'exportation
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(nomF));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void loadProds(List<Produit> lP, HttpServletResponse response, String qt, String pr){
        Document document = new Document();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        try {
            // Créer une police en gras
            Font pg = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font pd = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            String nomF= "ListeProduits_"+sdf.format(new Date())+".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(nomF));
            PdfFooterProdsEvent event = new PdfFooterProdsEvent();
            writer.setPageEvent(event);
            document.open();
            Paragraph ph = new Paragraph("Liste des Produits", pd);
            ph.setAlignment(Element.ALIGN_CENTER);
            document.add(ph);
            int nbC=6;
            if ((pr != null && qt == null) || (pr == null && qt != null)){
                nbC=5;
            }else if(pr == null && qt == null){
                nbC=4;
            }
            // Ajouter le tableau des détails de la facture
            PdfPTable table = new PdfPTable(nbC); //colonnes tt
            table.setSpacingBefore(20f);
            PdfPCell cc = new PdfPCell(new Phrase("Qte", pg));//tt
            table.setWidthPercentage(100);
            if (nbC==5){
                table.setWidths(new float[]{20,15,35,10, 20});//tt
            } else if(nbC==4){
                table.setWidths(new float[]{20,20,35, 25});//tt
            }
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            // Ajouter une cellule pour chaque détail de la facture
            table.addCell(new Phrase("Image", pg));//tt
            table.addCell(new Phrase("Référence", pg));
            table.addCell(new Phrase("Désignation", pg));
            if (nbC != 4 && qt != null) {
                table.addCell(cc);
            }
            if (nbC != 4 && pr != null) {
                table.addCell(new Phrase("Prix unitaire", pg));
            }
            table.addCell(new Phrase("Catégorie", pg));

            // Ajouter des lignes de données (à remplacer par vos propres données)
            for (Produit p : lP){
                Image image = Image.getInstance(p.getPic().getData());
                image.scaleToFit(20, 20);
                table.addCell(image);
                table.addCell(p.getRef());
                table.addCell(p.getName());
                if (nbC != 4 && qt != null) {
                    table.addCell(String.valueOf(p.getQuantite()));
                }
                if (nbC != 4 && pr != null) {
                    table.addCell(String.format("%.2f",p.getPrixVente()));
                }
                table.addCell(p.getCategorie().getLib());
            }
            /*for (Produit p : lP) {

                try {

                    System.out.println("Test image produit : "
                            + p.getRef() + " - " + p.getName());

                    if (p.getPic() == null) {
                        System.out.println("ERREUR : p.getPic() == null");
                    } else if (p.getPic().getData() == null) {
                        System.out.println("ERREUR : data == null");
                    } else {
                        System.out.println("Taille image : "
                                + p.getPic().getData().length + " octets");
                    }

                    Image image = Image.getInstance(p.getPic().getData());

                    image.scaleToFit(20, 20);
                    table.addCell(image);

                } catch (Exception e) {

                    System.out.println("********************************");
                    System.out.println("IMAGE INVALIDE !");
                    System.out.println("Référence : " + p.getRef());
                    System.out.println("Nom       : " + p.getName());
                    System.out.println("ID image  : " +
                            (p.getPic() != null ? p.getPic().getId() : "NULL"));
                    System.out.println("********************************");

                    e.printStackTrace();

                    throw e;
                }

                table.addCell(p.getRef());
                table.addCell(p.getName());

                if (nbC != 4 && qt != null) {
                    table.addCell(String.valueOf(p.getQuantite()));
                }

                if (nbC != 4 && pr != null) {
                    table.addCell(String.format("%.2f", p.getPrixVente()));
                }

                table.addCell(p.getCategorie().getLib());
            }*/

            // Ajouter le tableau au document
            document.add(table);
            //les montants
            document.close();


            // Définir les entêtes pour le téléchargement du fichier
            response.setContentType("application/pdf");
            String headerKey ="Content-Disposition";
            String headerValue = "attachment; filename="+ nomF;
            response.setHeader(headerKey, headerValue);

            // Copier le contenu du fichier PDF dans la réponse HTTP
            response.getOutputStream().write(org.apache.commons.io.IOUtils.toByteArray(new java.io.FileInputStream(nomF)));
            response.getOutputStream().flush();
            response.getOutputStream().close();

            // Supprimer le fichier local après l'exportation
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(nomF));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }


    public Facture creeFacture(Commande commande, Paiement paiement, Client client) {
        Facture facture = new Facture();
        SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy");
        String date = sdf.format(new Date());
        System.out.println("from creer facture : "+date);
        facture.setId(numeroFactureService.genererNouveauNumero());
        facture.setDate(date);
        facture.setCommande(commande);
        facture.setPaiement(paiement);
        facture.setClient(client);
        return this.repFacture.save(facture);
    }

    public void saveLF(LigneFacture lf) {
        this.repLigneFac.save(lf);
    }
    public Facture saveFac(Facture facture){
        return this.repFacture.save(facture);
    }

    public Facture getFactureByPaiement(Paiement paiement) {
        return this.repFacture.findByPaiement(paiement);
    }
    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY); // couleur fond gris
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); // centrer le texte
        cell.setPadding(5); // marges internes
        return cell;
    }
}
