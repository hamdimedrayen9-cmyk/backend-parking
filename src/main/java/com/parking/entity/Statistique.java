package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "statistiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "parking")
@EqualsAndHashCode(exclude = "parking")
public class Statistique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nombreReservations;
    private double revenus;

    @OneToOne
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;
}
