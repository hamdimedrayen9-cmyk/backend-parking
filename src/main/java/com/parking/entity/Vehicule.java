package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "vehicules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "client")
@EqualsAndHashCode(exclude = "client")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    private String marque;
    private String modele;

    @Column(nullable = false)
    private String type; // VOITURE, MOTO, CAMION, VAN

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}
