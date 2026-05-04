package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "parking")
@EqualsAndHashCode(exclude = "parking")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false)
    private boolean disponible = true;

    @Column(nullable = false)
    private String type; // STANDARD, HANDICAPE, MOTO, VIP

    @ManyToOne
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;
}
