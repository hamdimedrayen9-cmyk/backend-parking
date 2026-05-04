package com.parking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bienvenue sur ParkManager ! 🅿️");
        message.setText("Bonjour " + name + ",\n\n" +
                "Nous sommes ravis de vous accueillir sur ParkManager ! Votre compte a été créé avec succès.\n" +
                "Vous pouvez désormais réserver vos places de parking en toute simplicité.\n\n" +
                "À très bientôt,\n" +
                "L'équipe ParkManager.");
        
        try {
            mailSender.send(message);
            System.out.println("✅ Email de bienvenue envoyé à: " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    public void sendConfirmationEmail(String to, String name, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirmez votre compte ParkManager 🅿️");
        
        String confirmationUrl = "http://localhost:8087/confirm?token=" + token;
        
        message.setText("Bonjour " + name + ",\n\n" +
                "Merci de vous être inscrit sur ParkManager ! Veuillez cliquer sur le lien ci-dessous pour confirmer votre compte :\n\n" +
                confirmationUrl + "\n\n" +
                "Si vous n'êtes pas à l'origine de cette inscription, veuillez ignorer cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe ParkManager.");
        
        try {
            mailSender.send(message);
            System.out.println("✅ Email de confirmation envoyé à: " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de confirmation: " + e.getMessage());
        }
    }
}
