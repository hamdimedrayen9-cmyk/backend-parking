package com.parking.service;

import com.parking.entity.*;
import com.parking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private PaiementRepository paiementRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private TarifRepository tarifRepository;
    @Autowired private ParkingService parkingService;

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> findByClient(Client client) {
        return reservationRepository.findByClient(client);
    }

    public List<Reservation> findByParkingId(Long parkingId) {
        return reservationRepository.findByParkingId(parkingId);
    }

    public List<Reservation> findByParkingIdAndStatut(Long parkingId, String statut) {
        return reservationRepository.findByParkingIdAndStatut(parkingId, statut);
    }

    public List<Reservation> findByStatut(String statut) {
        return reservationRepository.findByStatut(statut);
    }

    public Reservation creerReservation(Client client, Place place, Vehicule vehicule,
                                         LocalDateTime dateDebut, LocalDateTime dateFin) {
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setPlace(place);
        reservation.setVehicule(vehicule);
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);
        reservation.setStatut("EN_ATTENTE");

        // Marquer la place comme indisponible
        place.setDisponible(false);
        placeRepository.save(place);

        // Créer un paiement en attente
        Reservation saved = reservationRepository.save(reservation);

        double montant = calculerMontant(place, vehicule, dateDebut, dateFin);
        Paiement paiement = new Paiement();
        paiement.setReservation(saved);
        paiement.setMontant(montant);
        paiement.setStatut("EN_ATTENTE");
        paiementRepository.save(paiement);

        return saved;
    }

    public double calculerMontant(Place place, Vehicule vehicule, LocalDateTime debut, LocalDateTime fin) {
        long heures = ChronoUnit.HOURS.between(debut, fin);
        if (heures < 1) heures = 1;

        Optional<Tarif> tarif = tarifRepository.findByParkingAndTypeVehicule(place.getParking(), vehicule.getType());
        double prixHeure = tarif.map(Tarif::getPrixHeure).orElse(5.0);
        return heures * prixHeure;
    }

    public Reservation valider(Long id) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        r.setStatut("VALIDEE");
        return reservationRepository.save(r);
    }

    public Reservation refuser(Long id) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        r.setStatut("REFUSEE");
        // Libérer la place
        r.getPlace().setDisponible(true);
        placeRepository.save(r.getPlace());
        return reservationRepository.save(r);
    }

    public Reservation payer(Long id) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        Paiement p = r.getPaiement();
        if (p != null) {
            p.setStatut("PAYE");
            p.setDatePaiement(LocalDateTime.now());
            paiementRepository.save(p);
            // Mettre à jour statistiques
            parkingService.updateStatistique(r.getPlace().getParking(), p.getMontant());
        }
        r.setStatut("TERMINEE");
        // Libérer la place
        r.getPlace().setDisponible(true);
        placeRepository.save(r.getPlace());
        return reservationRepository.save(r);
    }

    public Reservation annuler(Long id) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        r.setStatut("ANNULEE");
        r.getPlace().setDisponible(true);
        placeRepository.save(r.getPlace());
        return reservationRepository.save(r);
    }

    public void deleteById(Long id) {
        reservationRepository.findById(id).ifPresent(r -> {
            // Libérer la place si la réservation était active (EN_ATTENTE ou VALIDEE)
            if ("EN_ATTENTE".equals(r.getStatut()) || "VALIDEE".equals(r.getStatut())) {
                r.getPlace().setDisponible(true);
                placeRepository.save(r.getPlace());
            }
            
            // Si la réservation était terminée, mettre à jour les statistiques à l'envers
            if ("TERMINEE".equals(r.getStatut()) && r.getPaiement() != null) {
                parkingService.revertStatistique(r.getPlace().getParking(), r.getPaiement().getMontant());
            }

            // Supprimer explicitement le paiement pour éviter les violations de contraintes
            if (r.getPaiement() != null) {
                paiementRepository.delete(r.getPaiement());
                r.setPaiement(null);
            }
            
            reservationRepository.delete(r);
        });
    }

    public Map<String, Long> getReservationsByDay() {
        List<Reservation> all = reservationRepository.findAll();
        return all.stream()
            .collect(Collectors.groupingBy(
                r -> r.getDateDebut().toLocalDate().toString(),
                TreeMap::new,
                Collectors.counting()
            ));
    }

    public long countFreePlaces() {
        return placeRepository.countByDisponible(true);
    }

    public long countOccupiedPlaces() {
        return placeRepository.countByDisponible(false);
    }
}
