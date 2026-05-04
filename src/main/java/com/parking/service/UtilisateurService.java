package com.parking.service;

import com.parking.entity.*;
import com.parking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UtilisateurService {

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private AdminParkingRepository adminParkingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public Optional<Utilisateur> findByToken(String token) {
        return utilisateurRepository.findByConfirmationToken(token);
    }

    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    public Client registerClient(String nom, String email, String motDePasse) {
        Client client = new Client();
        client.setNom(nom);
        client.setEmail(email);
        client.setMotDePasse(passwordEncoder.encode(motDePasse));
        client.setRole("CLIENT");
        client.setEnabled(false); // Doit confirmer son compte
        client.setConfirmationToken(UUID.randomUUID().toString());
        return clientRepository.save(client);
    }

    public boolean confirmAccount(String token) {
        return utilisateurRepository.findByConfirmationToken(token)
            .map(u -> {
                u.setEnabled(true);
                u.setConfirmationToken(null);
                utilisateurRepository.save(u);
                return true;
            }).orElse(false);
    }

    public AdminParking createAdmin(String nom, String email, String motDePasse) {
        AdminParking admin = new AdminParking();
        admin.setNom(nom);
        admin.setEmail(email);
        admin.setMotDePasse(passwordEncoder.encode(motDePasse));
        admin.setRole("ADMIN_PARKING");
        return adminParkingRepository.save(admin);
    }

    public void deleteById(Long id) {
        try {
            utilisateurRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de supprimer cet utilisateur.");
        }
    }

    public List<AdminParking> findAllAdmins() {
        return adminParkingRepository.findAll();
    }

    public List<Client> findAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> findClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public Optional<AdminParking> findAdminByEmail(String email) {
        return adminParkingRepository.findByEmail(email);
    }
}
