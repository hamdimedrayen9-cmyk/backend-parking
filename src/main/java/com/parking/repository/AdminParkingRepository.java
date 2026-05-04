package com.parking.repository;
import com.parking.entity.AdminParking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AdminParkingRepository extends JpaRepository<AdminParking, Long> {
    Optional<AdminParking> findByEmail(String email);
}
