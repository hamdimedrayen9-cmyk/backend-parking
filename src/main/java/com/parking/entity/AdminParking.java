package com.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "admins_parking")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = "parking")
@EqualsAndHashCode(callSuper = true, exclude = "parking")
public class AdminParking extends Utilisateur {

    @OneToOne(mappedBy = "adminParking")
    private Parking parking;
}
