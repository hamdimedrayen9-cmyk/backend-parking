package com.parking.repository;
import com.parking.entity.Paiement;
import com.parking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByReservation(Reservation reservation);
}
