package com.parking.repository;
import com.parking.entity.Vehicule;
import com.parking.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    List<Vehicule> findByClient(Client client);
    boolean existsByImmatriculation(String immatriculation);
}
