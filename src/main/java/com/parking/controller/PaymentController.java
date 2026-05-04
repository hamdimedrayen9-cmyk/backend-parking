package com.parking.controller;

import com.parking.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, String> payload) {
        try {
            Long reservationId = Long.parseLong(payload.get("reservationId"));
            String cardNumber = payload.get("cardNumber");
            String expiryDate = payload.get("expiryDate");
            String cvc = payload.get("cvc");

            Map<String, Object> result = paymentService.processPayment(reservationId, cardNumber, expiryDate, cvc);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Erreur lors du traitement du paiement: " + e.getMessage()
            ));
        }
    }
}
