package com.parking.service;

import com.parking.entity.*;
import com.parking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParkingService {

    @Autowired private ParkingRepository parkingRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private TarifRepository tarifRepository;
    @Autowired private StatistiqueRepository statistiqueRepository;

    public List<Parking> findAll() {
        return parkingRepository.findAll();
    }

    public Optional<Parking> findById(Long id) {
        return parkingRepository.findById(id);
    }

    public Parking save(Parking parking) {
        Parking saved = parkingRepository.save(parking);
        // Créer statistique automatiquement
        if (statistiqueRepository.findByParking(saved).isEmpty()) {
            Statistique stat = new Statistique();
            stat.setParking(saved);
            stat.setNombreReservations(0);
            stat.setRevenus(0.0);
            statistiqueRepository.save(stat);
        }
        return saved;
    }

    public void deleteById(Long id) {
        parkingRepository.deleteById(id);
    }

    public Optional<Parking> findByAdminParking(AdminParking admin) {
        return parkingRepository.findByAdminParking(admin);
    }

    // Places
    public List<Place> findPlacesByParking(Parking parking) {
        return placeRepository.findByParking(parking);
    }

    public Place savePlace(Place place) {
        return placeRepository.save(place);
    }

    public Optional<Place> findPlaceById(Long id) {
        return placeRepository.findById(id);
    }

    public void deletePlaceById(Long id) {
        placeRepository.deleteById(id);
    }

    public List<Place> findDisponiblesByParking(Parking parking) {
        return placeRepository.findByParkingAndDisponible(parking, true);
    }

    // Tarifs
    public List<Tarif> findTarifsByParking(Parking parking) {
        return tarifRepository.findByParking(parking);
    }

    public Tarif saveTarif(Tarif tarif) {
        return tarifRepository.save(tarif);
    }

    public Optional<Tarif> findTarifById(Long id) {
        return tarifRepository.findById(id);
    }

    public void deleteTarifById(Long id) {
        tarifRepository.deleteById(id);
    }

    public Optional<Tarif> findTarifByParkingAndType(Parking parking, String typeVehicule) {
        return tarifRepository.findByParkingAndTypeVehicule(parking, typeVehicule);
    }

    // Statistiques
    public Statistique getOrCreateStatistique(Parking parking) {
        return statistiqueRepository.findByParking(parking).orElseGet(() -> {
            Statistique stat = new Statistique();
            stat.setParking(parking);
            stat.setNombreReservations(0);
            stat.setRevenus(0.0);
            return statistiqueRepository.save(stat);
        });
    }

    public void updateStatistique(Parking parking, double montant) {
        Statistique stat = getOrCreateStatistique(parking);
        stat.setNombreReservations(stat.getNombreReservations() + 1);
        stat.setRevenus(stat.getRevenus() + montant);
        statistiqueRepository.save(stat);
    }

    public void revertStatistique(Parking parking, double montant) {
        Statistique stat = getOrCreateStatistique(parking);
        if (stat.getNombreReservations() > 0) {
            stat.setNombreReservations(stat.getNombreReservations() - 1);
        }
        stat.setRevenus(Math.max(0, stat.getRevenus() - montant));
        statistiqueRepository.save(stat);
    }

    public List<Statistique> findAllStatistiques() {
        return statistiqueRepository.findAll();
    }
}
