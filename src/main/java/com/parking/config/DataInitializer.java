package com.parking.config;

import com.parking.entity.SuperAdmin;
import com.parking.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer le SuperAdmin par défaut s'il n'existe pas
        if (!utilisateurRepository.existsByEmail("superadmin@parking.com")) {
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setNom("Super Administrateur");
            superAdmin.setEmail("superadmin@parking.com");
            superAdmin.setMotDePasse(passwordEncoder.encode("admin123"));
            superAdmin.setRole("SUPERADMIN");
            superAdmin.setEnabled(true);
            utilisateurRepository.save(superAdmin);
            System.out.println("✅ SuperAdmin créé: superadmin@parking.com / admin123");
        }
    }
}
