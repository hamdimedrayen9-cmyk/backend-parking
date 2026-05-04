package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "parkings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"places", "tarifs", "statistique", "adminParking"})
@EqualsAndHashCode(exclude = {"places", "tarifs", "statistique", "adminParking"})
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false)
    private int capacite;

    private Double latitude;
    private Double longitude;

    @OneToOne
    @JoinColumn(name = "admin_parking_id")
    private AdminParking adminParking;

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Place> places;

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tarif> tarifs;

    @OneToOne(mappedBy = "parking", cascade = CascadeType.ALL)
    private Statistique statistique;
}
