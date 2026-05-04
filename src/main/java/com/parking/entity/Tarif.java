package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "tarifs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "parking")
@EqualsAndHashCode(exclude = "parking")
public class Tarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double prixHeure;

    @Column(nullable = false)
    private String typeVehicule; // VOITURE, MOTO, CAMION, VAN

    @ManyToOne
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;
}
