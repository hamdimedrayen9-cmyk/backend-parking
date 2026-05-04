package com.parking.service;

import com.parking.entity.Paiement;
import com.parking.entity.Reservation;
import com.parking.repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PaiementRepository paiementRepository;

    /**
     * Simule un processus de paiement externe.
     * Dans une application réelle, on appellerait ici une API comme Stripe, PayPal ou Flouci.
     */
    public Map<String, Object> processPayment(Long reservationId, String cardNumber, String expiryDate, String cvc) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Reservation reservation = reservationService.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

            if (!"VALIDEE".equals(reservation.getStatut())) {
                response.put("success", false);
                response.put("message", "La réservation n'est pas prête pour le paiement (Statut actuel: " + reservation.getStatut() + ")");
                return response;
            }

            // Simulation de validation de carte (très basique)
            if (cardNumber == null || cardNumber.length() < 16) {
                response.put("success", false);
                response.put("message", "Numéro de carte invalide");
                return response;
            }

            // Si tout est ok, on effectue le paiement dans notre système
            System.out.println("Processing payment for reservation: " + reservationId);
            reservationService.payer(reservationId);
            System.out.println("Payment processed successfully for reservation: " + reservationId);
            
            response.put("success", true);
            response.put("message", "Paiement de " + (reservation.getPaiement() != null ? reservation.getPaiement().getMontant() : 0) + " DT accepté.");
            response.put("transactionId", "TXN-" + System.currentTimeMillis());
            
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur technique: " + e.getMessage());
        }
        
        return response;
    }
}
