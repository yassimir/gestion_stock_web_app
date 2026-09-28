package com.gharnata.service;

import com.gharnata.compenents.ResourceUtils;
import com.gharnata.entity.Image;
import com.gharnata.repository.RepImage;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Service
public class ServImage {
    private final RepImage repImage;

    // Taille maximale souhaitée : 300 Ko
    private static final int MAX_SIZE_KB = 300;
    private static final int MAX_SIZE_BYTES = MAX_SIZE_KB * 1024;

    // Dimension maximale
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;

    public ServImage(RepImage repImage) {
        this.repImage = repImage;
    }
    /*public Image treatPic(MultipartFile imagee) throws Exception {

        System.out.println("========== IMAGE ==========");
        System.out.println("imagee = " + imagee);

        if (imagee != null) {
            System.out.println("Nom : " + imagee.getOriginalFilename());
            System.out.println("Taille : " + imagee.getSize());
            System.out.println("Type : " + imagee.getContentType());
            System.out.println("Vide : " + imagee.isEmpty());
        }

        Image image = new Image();

        if (imagee != null && !imagee.isEmpty()) {

            byte[] data = imagee.getBytes();

            System.out.println("Image reçue : " + data.length + " octets");

            image.setData(data);
            return repImage.save(image);
        }

        System.out.println("AUCUNE IMAGE -> image par défaut");

        byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
        image.setData(dt);

        return repImage.save(image);
    }*/

    public Image treatPic(MultipartFile imagee) throws Exception {

        Image image = new Image();

        // Aucune image
        if (imagee == null || imagee.isEmpty()) {

            System.out.println("Aucune image -> image par défaut");

            byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
            image.setData(dt);

            return repImage.save(image);
        }

        // Image reçue
        byte[] originalData = imagee.getBytes();

        System.out.println("Image originale : "
                + originalData.length / 1024 + " KB");

        // Si <= 300 KB : on garde l'originale
        if (originalData.length <= 300 * 1024) {

            System.out.println("Image <= 300 KB -> originale");

            image.setData(originalData);

            return repImage.save(image);
        }

        // Image > 300 KB
        System.out.println("Image > 300 KB -> compression");

        BufferedImage original = ImageIO.read(
                new ByteArrayInputStream(originalData)
        );

        if (original == null) {
            System.out.println("Image illisible -> image par défaut");

            byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
            image.setData(dt);

            return repImage.save(image);
        }

        // Redimensionnement maximum 800x800
        BufferedImage resized = Scalr.resize(
                original,
                Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC,
                800,
                800
        );

        // Compression JPEG
        byte[] compressedData = compressJPEG(resized, 0.80f);

        System.out.println("Après compression : "
                + compressedData.length / 1024 + " KB");

        // Si encore trop grande, on diminue progressivement la qualité
        float quality = 0.75f;

        while (compressedData.length > 300 * 1024 && quality >= 0.30f) {

            compressedData = compressJPEG(resized, quality);

            System.out.println(
                    "Qualité : " + quality +
                            " -> " + compressedData.length / 1024 + " KB"
            );

            quality -= 0.05f;
        }

        image.setData(compressedData);

        return repImage.save(image);
    }
    //Compression JPEG avec qualité configurable

    private byte[] compressJPEG(BufferedImage original, float quality)
            throws IOException {

        // Créer une image RGB compatible JPEG
        BufferedImage rgbImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = rgbImage.createGraphics();

        // Fond blanc pour les images avec transparence
        graphics.setColor(Color.WHITE);
        graphics.fillRect(
                0,
                0,
                original.getWidth(),
                original.getHeight()
        );

        graphics.drawImage(original, 0, 0, null);
        graphics.dispose();

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext()) {
            throw new IOException("Aucun JPEG writer disponible");
        }

        ImageWriter writer = writers.next();

        ImageWriteParam param =
                writer.getDefaultWriteParam();

        param.setCompressionMode(
                ImageWriteParam.MODE_EXPLICIT
        );

        param.setCompressionQuality(quality);

        try (ImageOutputStream imageOutputStream =
                     ImageIO.createImageOutputStream(outputStream)) {

            writer.setOutput(imageOutputStream);

            writer.write(
                    null,
                    new IIOImage(rgbImage, null, null),
                    param
            );

        } finally {
            writer.dispose();
        }

        return outputStream.toByteArray();
    }
   /* public String treatPic(MultipartFile image){
        if (!image.isEmpty()) {
            System.out.printf("hello from image");
            try {
                // Sauvegarde le fichier dans le dossier d'upload de l'application
                Date date = new Date();
                SimpleDateFormat formater = new SimpleDateFormat("MMddHHmmss");
                String nm = formater.format(date);
                String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
                String fileName = nm + "." + ext;
                *//*File directory = new File("src/main/resources/static/images/products");*//* //chemin relatif por int
                File directory = new File("classes/static/images/products");
                File dest = new File(directory.getAbsolutePath() + File.separator + fileName);
                image.transferTo(dest);
                return fileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }*/
    /*public Image treatPic(MultipartFile imagee) throws Exception {
        Image image = new Image();
        if (!imagee.isEmpty()) {
            try {
                byte[] data = imagee.getBytes();
                image.setData(data);
                return repImage.save(image);
            } catch (IOException e) {
                //byte[] dt = Files.readAllBytes(path);
                byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
                image.setData(dt);
                return repImage.save(image);
            }
        }
        byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
        image.setData(dt);
        return repImage.save(image);
    }*/
   /*public Image treatPic(MultipartFile imagee) throws Exception {
       Image image = new Image();

       if (!imagee.isEmpty()) {
           try {
               BufferedImage original = ImageIO.read(imagee.getInputStream());

               if (original == null) {
                   throw new IOException("Image invalide");
               }

               // Redimensionnement : maximum 800 px
               BufferedImage resized = Scalr.resize(
                       original,
                       Scalr.Method.QUALITY,
                       Scalr.Mode.AUTOMATIC,
                       800,
                       800
               );

               // Compression JPEG
               ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

               ImageIO.write(resized, "jpg", outputStream);

               image.setData(outputStream.toByteArray());

               return repImage.save(image);

           } catch (IOException e) {
               byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
               image.setData(dt);
               return repImage.save(image);
           }
       }

       byte[] dt = ResourceUtils.load("static/images/defaut.JPEG");
       image.setData(dt);
       return repImage.save(image);
   }*/
    /*public void modifyPic(MultipartFile image, long id){
        Image img = this.repImage.findById(id).orElse(new Image());
        if (!image.isEmpty()) {
            try {
                byte[] data = image.getBytes();
                img.setData(data);
                repImage.save(img);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
   public void modifyPic(MultipartFile image, long id) {

       Image img = this.repImage.findById(id).orElse(new Image());

       // Aucune nouvelle image sélectionnée
       if (image == null || image.isEmpty()) {
           System.out.println("Aucune nouvelle image -> conservation de l'ancienne");
           return;
       }

       try {

           // Image originale
           byte[] originalData = image.getBytes();

           System.out.println("Nouvelle image : "
                   + originalData.length / 1024 + " KB");

           byte[] finalData;

           // Si <= 300 KB : garder l'originale
           if (originalData.length <= 300 * 1024) {

               System.out.println("Image <= 300 KB -> originale");

               finalData = originalData;

           } else {

               System.out.println("Image > 300 KB -> compression");

               BufferedImage original = ImageIO.read(
                       new ByteArrayInputStream(originalData)
               );

               if (original == null) {
                   System.out.println("Image illisible -> aucune modification");
                   return;
               }

               // Redimensionnement maximum 800x800
               BufferedImage resized = Scalr.resize(
                       original,
                       Scalr.Method.QUALITY,
                       Scalr.Mode.AUTOMATIC,
                       800,
                       800
               );

               // Première compression
               finalData = compressJPEG(resized, 0.80f);

               System.out.println("Après compression : "
                       + finalData.length / 1024 + " KB");

               // Réduction progressive de la qualité
               float quality = 0.75f;

               while (finalData.length > 300 * 1024
                       && quality >= 0.30f) {

                   finalData = compressJPEG(resized, quality);

                   System.out.println(
                           "Qualité : " + quality
                                   + " -> "
                                   + finalData.length / 1024
                                   + " KB"
                   );

                   quality -= 0.05f;
               }
           }

           // Remplacer l'ancienne image
           img.setData(finalData);

           repImage.save(img);

           System.out.println("Image modifiée avec succès.");

       } catch (IOException e) {

           System.out.println("Erreur lors du traitement de l'image :");
           e.printStackTrace();
       }
   }

}
