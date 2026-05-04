package com.parking.repository;
import com.parking.entity.Parking;
import com.parking.entity.AdminParking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ParkingRepository extends JpaRepository<Parking, Long> {
    Optional<Parking> findByAdminParking(AdminParking admin);
}
