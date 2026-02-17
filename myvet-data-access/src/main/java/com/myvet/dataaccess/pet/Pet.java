package com.myvet.dataaccess.pet;

import com.myvet.dataaccess.enums.Enums.HealthStatus;
import com.myvet.dataaccess.baseentity.BaseEntity;
import com.myvet.dataaccess.owner.Owner;
import com.myvet.dataaccess.vet.Vet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vid", nullable = false)
    private Vet vet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String species;

    @Column(nullable = false, length = 100)
    private String breed;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(length = 50)
    private String color;

    @Column(length = 200)
    private String markings;

    @Column(name = "cip_id", unique = true, length = 50)
    private String cipId;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false, length = 20)
    @Builder.Default
    private HealthStatus healthStatus = HealthStatus.healthy;

    @Column(name = "vet_auth_uid", length = 128)
    private String vetAuthUid;
}