package com.parking.repository;
import com.parking.entity.Place;
import com.parking.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByParking(Parking parking);
    List<Place> findByParkingAndDisponible(Parking parking, boolean disponible);
    List<Place> findByParkingAndType(Parking parking, String type);
    long countByDisponible(boolean disponible);
}
