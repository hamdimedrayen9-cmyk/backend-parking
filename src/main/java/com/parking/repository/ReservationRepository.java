package com.parking.repository;
import com.parking.entity.Reservation;
import com.parking.entity.Client;
import com.parking.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClient(Client client);
    List<Reservation> findByStatut(String statut);
    List<Reservation> findByPlace(Place place);
    @Query("SELECT r FROM Reservation r WHERE r.place.parking.id = :parkingId")
    List<Reservation> findByParkingId(Long parkingId);
    @Query("SELECT r FROM Reservation r WHERE r.place.parking.id = :parkingId AND r.statut = :statut")
    List<Reservation> findByParkingIdAndStatut(Long parkingId, String statut);

    @Query("SELECT r.dateDebut, COUNT(r) FROM Reservation r GROUP BY r.dateDebut ORDER BY r.dateDebut ASC")
    List<Object[]> countReservationsByDay();
}
