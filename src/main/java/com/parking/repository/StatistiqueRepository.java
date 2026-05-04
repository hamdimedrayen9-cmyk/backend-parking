package com.parking.repository;
import com.parking.entity.Statistique;
import com.parking.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StatistiqueRepository extends JpaRepository<Statistique, Long> {
    Optional<Statistique> findByParking(Parking parking);
}
