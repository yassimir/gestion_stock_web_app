package com.gharnata.compenents;

import com.gharnata.entity.Categorie;
import com.gharnata.entity.FactureSequence;
import com.gharnata.entity.Utilisateur;
import com.gharnata.enums.Role;
import com.gharnata.repository.RepCategory;
import com.gharnata.repository.RepFacSequ;
import com.gharnata.repository.UtilisateurRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {
    private final RepCategory categoryRepository;
    private final RepFacSequ repFacSequ;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RepCategory categoryRepository, RepFacSequ repFacSequ,
                           UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.repFacSequ = repFacSequ;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (!categoryRepository.existsByLib("sans Catégorie")) {
            Categorie cat = new Categorie("sans Catégorie");
            categoryRepository.save(cat);
        }
        if (repFacSequ.findAll().isEmpty()) {
            FactureSequence factureSequence = new FactureSequence();
            factureSequence.setDernierNumero(0);
            repFacSequ.save(factureSequence);
        }
        if (utilisateurRepository.findByUsername("admin").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setUsername("admin");
            admin.setMotDePasse(passwordEncoder.encode("changeMoi123"));
            admin.setNom("Administrateur");
            admin.setRole(Role.ADMIN);
            utilisateurRepository.save(admin);
        }
    }
}
