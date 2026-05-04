package com.parking.service;

import com.parking.entity.Parking;
import com.parking.entity.Place;
import com.parking.entity.Tarif;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service d'Intelligence Artificielle pour la recommandation de parking.
 *
 * Algorithme de scoring pondéré multi-critères :
 *   - Distance  (poids par défaut : 40%) → Formule Haversine
 *   - Prix      (poids par défaut : 35%) → Prix/heure du type de véhicule
 *   - Dispo     (poids par défaut : 25%) → Taux de places libres
 *
 * Chaque score est normalisé entre 0 et 100.
 * Le score final = distScore × wDist + prixScore × wPrix + dispoScore × wDispo
 */
@Service
public class ParkingRecommendationService {

    @Autowired
    private ParkingService parkingService;

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcule la distance en km entre deux coordonnées GPS (formule Haversine).
     */
    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Génère la liste des recommandations triées par score décroissant.
     *
     * @param userLat       Latitude de l'utilisateur
     * @param userLng       Longitude de l'utilisateur
     * @param typeVehicule  Type de véhicule (VOITURE, MOTO, CAMION, VAN)
     * @param wDist         Poids distance (0–100)
     * @param wPrix         Poids prix     (0–100)
     * @param wDispo        Poids dispo    (0–100)
     * @return Liste triée de recommandations
     */
    public List<ParkingRecommendationDTO> recommander(
            double userLat, double userLng, String typeVehicule,
            double wDist, double wPrix, double wDispo) {

        // Normaliser les poids (somme = 1)
        double total = wDist + wPrix + wDispo;
        if (total == 0) { wDist = 40; wPrix = 35; wDispo = 25; total = 100; }
        double wd = wDist / total;
        double wp = wPrix / total;
        double wv = wDispo / total;

        List<Parking> parkings = parkingService.findAll();

        // Pré-calculer les données brutes
        List<ParkingRecommendationDTO> dtos = new ArrayList<>();
        for (Parking p : parkings) {
            if (p.getLatitude() == null || p.getLongitude() == null) continue;

            ParkingRecommendationDTO dto = new ParkingRecommendationDTO();
            dto.setId(p.getId());
            dto.setNom(p.getNom());
            dto.setAdresse(p.getAdresse());
            dto.setLatitude(p.getLatitude());
            dto.setLongitude(p.getLongitude());

            // Distance
            double dist = haversine(userLat, userLng, p.getLatitude(), p.getLongitude());
            dto.setDistanceKm(Math.round(dist * 100.0) / 100.0);

            // Prix/heure (pour le type de véhicule)
            double prix = getPrixHeure(p, typeVehicule);
            dto.setPrixHeure(prix);

            // Places disponibles
            List<Place> dispo = parkingService.findDisponiblesByParking(p);
            dto.setPlacesDisponibles(dispo.size());
            dto.setCapaciteTotale(p.getCapacite());

            dtos.add(dto);
        }

        if (dtos.isEmpty()) return dtos;

        // Normalisation de chaque critère entre 0 et 100
        normaliserScores(dtos);

        // Score final pondéré
        for (ParkingRecommendationDTO dto : dtos) {
            double scoreTotal = dto.getScoreDistance() * wd
                    + dto.getScorePrix() * wp
                    + dto.getScoreDisponibilite() * wv;
            dto.setScoreTotal(Math.round(scoreTotal * 10.0) / 10.0);
        }

        // Trier par score décroissant
        dtos.sort(Comparator.comparingDouble(ParkingRecommendationDTO::getScoreTotal).reversed());

        // Attribuer rang et explication
        for (int i = 0; i < dtos.size(); i++) {
            ParkingRecommendationDTO dto = dtos.get(i);
            dto.setRang(i + 1);
            dto.setNiveauRecommandation(getNiveau(dto.getScoreTotal()));
            dto.setExplication(genererExplication(dto, i + 1));
        }

        return dtos;
    }

    /**
     * Retourne le prix/heure pour le type de véhicule donné.
     * Si aucun tarif correspondant, utilise le premier disponible ou 0.
     */
    private double getPrixHeure(Parking p, String typeVehicule) {
        List<Tarif> tarifs = parkingService.findTarifsByParking(p);
        if (tarifs.isEmpty()) return 0.0;

        // Chercher le tarif correspondant au type
        return tarifs.stream()
                .filter(t -> t.getTypeVehicule().equalsIgnoreCase(typeVehicule))
                .mapToDouble(Tarif::getPrixHeure)
                .findFirst()
                .orElse(tarifs.get(0).getPrixHeure());
    }

    /**
     * Normalise les scores de chaque critère entre 0 et 100.
     * Distance : plus faible = meilleur score (inversé)
     * Prix     : plus faible = meilleur score (inversé)
     * Dispo    : plus élevé = meilleur score
     */
    private void normaliserScores(List<ParkingRecommendationDTO> dtos) {
        // Distance
        double minDist = dtos.stream().mapToDouble(ParkingRecommendationDTO::getDistanceKm).min().orElse(0);
        double maxDist = dtos.stream().mapToDouble(ParkingRecommendationDTO::getDistanceKm).max().orElse(1);

        // Prix
        double minPrix = dtos.stream().mapToDouble(ParkingRecommendationDTO::getPrixHeure).min().orElse(0);
        double maxPrix = dtos.stream().mapToDouble(ParkingRecommendationDTO::getPrixHeure).max().orElse(1);

        // Disponibilité (en taux)
        // Note: on compare placesDisponibles / capaciteTotale
        double maxTauxDispo = dtos.stream()
                .mapToDouble(d -> d.getCapaciteTotale() > 0 ? (double) d.getPlacesDisponibles() / d.getCapaciteTotale() : 0)
                .max().orElse(1);

        for (ParkingRecommendationDTO dto : dtos) {
            // Score distance (inversé : moins loin = meilleur)
            double scoreDist;
            if (maxDist == minDist) {
                scoreDist = 100.0;
            } else {
                scoreDist = 100.0 * (1.0 - (dto.getDistanceKm() - minDist) / (maxDist - minDist));
            }
            dto.setScoreDistance(Math.round(scoreDist * 10.0) / 10.0);

            // Score prix (inversé : moins cher = meilleur)
            double scorePrix;
            if (maxPrix == minPrix) {
                scorePrix = 100.0;
            } else {
                scorePrix = 100.0 * (1.0 - (dto.getPrixHeure() - minPrix) / (maxPrix - minPrix));
            }
            dto.setScorePrix(Math.round(scorePrix * 10.0) / 10.0);

            // Score disponibilité
            double tauxDispo = dto.getCapaciteTotale() > 0
                    ? (double) dto.getPlacesDisponibles() / dto.getCapaciteTotale() : 0;
            double scoreDispo = maxTauxDispo > 0 ? 100.0 * (tauxDispo / maxTauxDispo) : 0;
            dto.setScoreDisponibilite(Math.round(scoreDispo * 10.0) / 10.0);
        }
    }

    /**
     * Détermine le niveau de recommandation basé sur le score total.
     */
    private String getNiveau(double score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "BON";
        if (score >= 40) return "MOYEN";
        return "FAIBLE";
    }

    /**
     * Génère une explication textuelle intelligente pour chaque recommandation.
     */
    private String genererExplication(ParkingRecommendationDTO dto, int rang) {
        StringBuilder sb = new StringBuilder();

        if (rang == 1) {
            sb.append("🏆 Meilleure option globale. ");
        } else if (rang == 2) {
            sb.append("🥈 Excellent choix alternatif. ");
        } else if (rang == 3) {
            sb.append("🥉 Bonne option à considérer. ");
        }

        // Distance
        if (dto.getDistanceKm() < 0.5) {
            sb.append("📍 Très proche (").append(String.format("%.0f", dto.getDistanceKm() * 1000)).append(" m). ");
        } else if (dto.getDistanceKm() < 2.0) {
            sb.append("📍 À ").append(String.format("%.1f", dto.getDistanceKm())).append(" km. ");
        } else {
            sb.append("📍 Éloigné (").append(String.format("%.1f", dto.getDistanceKm())).append(" km). ");
        }

        // Prix
        if (dto.getPrixHeure() == 0) {
            sb.append("💰 Tarif non défini. ");
        } else if (dto.getScorePrix() >= 70) {
            sb.append("💰 Tarif attractif (").append(String.format("%.2f", dto.getPrixHeure())).append(" DT/h). ");
        } else if (dto.getScorePrix() >= 40) {
            sb.append("💰 Tarif modéré (").append(String.format("%.2f", dto.getPrixHeure())).append(" DT/h). ");
        } else {
            sb.append("💰 Tarif élevé (").append(String.format("%.2f", dto.getPrixHeure())).append(" DT/h). ");
        }

        // Disponibilité
        if (dto.getPlacesDisponibles() == 0) {
            sb.append("❌ Complet en ce moment.");
        } else if (dto.getScoreDisponibilite() >= 70) {
            sb.append("✅ ").append(dto.getPlacesDisponibles()).append(" places disponibles.");
        } else {
            sb.append("⚠️ Seulement ").append(dto.getPlacesDisponibles()).append(" place(s) restante(s).");
        }

        return sb.toString();
    }
}
