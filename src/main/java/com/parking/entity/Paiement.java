package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "reservation")
@EqualsAndHashCode(exclude = "reservation")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double montant;

    private LocalDateTime datePaiement;

    @Column(nullable = false)
    private String statut; // EN_ATTENTE, PAYE, REMBOURSE

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
}
