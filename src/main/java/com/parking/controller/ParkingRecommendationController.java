package com.parking.controller;

import com.parking.service.ParkingRecommendationDTO;
import com.parking.service.ParkingRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la fonctionnalité d'IA de recommandation de parking.
 */
@Controller
@RequestMapping("/client/ai")
public class ParkingRecommendationController {

    @Autowired
    private ParkingRecommendationService recommendationService;

    /**
     * Page HTML de recommandation IA.
     */
    @GetMapping
    public String pageRecommandation() {
        return "client/ai-recommandation";
    }

    /**
     * API REST – retourne la liste des parkings recommandés en JSON.
     *
     * @param lat           Latitude de l'utilisateur
     * @param lng           Longitude de l'utilisateur
     * @param typeVehicule  Type de véhicule (VOITURE, MOTO, CAMION, VAN)
     * @param wDist         Poids distance (0–100, défaut 40)
     * @param wPrix         Poids prix (0–100, défaut 35)
     * @param wDispo        Poids disponibilité (0–100, défaut 25)
     */
    @GetMapping("/recommander")
    @ResponseBody
    public ResponseEntity<?> recommander(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "VOITURE") String typeVehicule,
            @RequestParam(defaultValue = "40") double wDist,
            @RequestParam(defaultValue = "35") double wPrix,
            @RequestParam(defaultValue = "25") double wDispo) {
        try {
            List<ParkingRecommendationDTO> results =
                    recommendationService.recommander(lat, lng, typeVehicule, wDist, wPrix, wDispo);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
