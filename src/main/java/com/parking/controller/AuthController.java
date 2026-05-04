package com.parking.controller;

import com.parking.entity.Client;
import com.parking.service.UtilisateurService;
import com.parking.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model, HttpSession session) {
        if (error != null) {
            String sessionError = (String) session.getAttribute("error");
            if (sessionError != null) {
                model.addAttribute("error", sessionError);
                session.removeAttribute("error");
            } else {
                model.addAttribute("error", "Email ou mot de passe incorrect.");
            }
        }
        if (logout != null) model.addAttribute("message", "Déconnexion réussie.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping({"/register", "/register/"})
    public String register(@RequestParam String nom,
                           @RequestParam String email,
                           @RequestParam String motDePasse,
                           RedirectAttributes ra) {
        if (utilisateurService.existsByEmail(email)) {
            ra.addFlashAttribute("error", "Cet email est déjà utilisé.");
            return "redirect:/register";
        }
        try {
            Client client = utilisateurService.registerClient(nom, email, motDePasse);
            System.out.println("👤 Nouvel utilisateur enregistré: " + email + " (Token: " + client.getConfirmationToken() + ")");
            
            // Envoi d'email de confirmation
            emailService.sendConfirmationEmail(email, nom, client.getConfirmationToken());
            
            ra.addFlashAttribute("success", "Compte créé avec succès ! Veuillez vérifier votre boîte mail (Mailpit) pour confirmer votre compte.");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());
            ra.addFlashAttribute("error", "Une erreur est survenue lors de l'inscription. Veuillez réessayer.");
            return "redirect:/register";
        }
        return "redirect:/login";
    }

    @GetMapping({"/confirm", "/confirm/"})
    public String confirmAccount(@RequestParam String token, RedirectAttributes ra) {
        System.out.println("🔍 Tentative de confirmation avec le jeton: " + token);
        if (utilisateurService.confirmAccount(token)) {
            System.out.println("✅ Compte confirmé avec succès pour le jeton: " + token);
            ra.addFlashAttribute("success", "Votre compte a été confirmé avec succès ! Vous pouvez maintenant vous connecter.");
        } else {
            System.err.println("❌ Échec de la confirmation pour le jeton: " + token);
            ra.addFlashAttribute("error", "Le lien de confirmation est invalide ou a expiré.");
        }
        return "redirect:/login";
    }
}
