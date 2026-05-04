package com.parking.repository;
import com.parking.entity.Tarif;
import com.parking.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TarifRepository extends JpaRepository<Tarif, Long> {
    List<Tarif> findByParking(Parking parking);
    Optional<Tarif> findByParkingAndTypeVehicule(Parking parking, String typeVehicule);
}
