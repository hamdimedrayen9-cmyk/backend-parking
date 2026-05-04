package com.parking.service;

/**
 * DTO de recommandation IA d'un parking.
 * Contient les scores normalisés et une explication générée.
 */
public class ParkingRecommendationDTO {

    private Long id;
    private String nom;
    private String adresse;
    private Double latitude;
    private Double longitude;

    // Données brutes
    private double distanceKm;
    private double prixHeure;
    private int placesDisponibles;
    private int capaciteTotale;

    // Scores normalisés (0–100)
    private double scoreDistance;
    private double scorePrix;
    private double scoreDisponibilite;
    private double scoreTotal;

    // Classement et explication
    private int rang;
    private String explication;
    private String niveauRecommandation; // EXCELLENT, BON, MOYEN, FAIBLE

    public ParkingRecommendationDTO() {}

    // ============ Getters & Setters ============

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getPrixHeure() { return prixHeure; }
    public void setPrixHeure(double prixHeure) { this.prixHeure = prixHeure; }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public int getCapaciteTotale() { return capaciteTotale; }
    public void setCapaciteTotale(int capaciteTotale) { this.capaciteTotale = capaciteTotale; }

    public double getScoreDistance() { return scoreDistance; }
    public void setScoreDistance(double scoreDistance) { this.scoreDistance = scoreDistance; }

    public double getScorePrix() { return scorePrix; }
    public void setScorePrix(double scorePrix) { this.scorePrix = scorePrix; }

    public double getScoreDisponibilite() { return scoreDisponibilite; }
    public void setScoreDisponibilite(double scoreDisponibilite) { this.scoreDisponibilite = scoreDisponibilite; }

    public double getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(double scoreTotal) { this.scoreTotal = scoreTotal; }

    public int getRang() { return rang; }
    public void setRang(int rang) { this.rang = rang; }

    public String getExplication() { return explication; }
    public void setExplication(String explication) { this.explication = explication; }

    public String getNiveauRecommandation() { return niveauRecommandation; }
    public void setNiveauRecommandation(String niveauRecommandation) { this.niveauRecommandation = niveauRecommandation; }
}
