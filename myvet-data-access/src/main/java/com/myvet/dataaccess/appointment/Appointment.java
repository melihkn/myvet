package com.myvet.dataaccess.appointment;

import com.myvet.dataaccess.enums.Enums.AppointmentStatus;
import com.myvet.dataaccess.baseentity.BaseEntity;
import com.myvet.dataaccess.owner.Owner;
import com.myvet.dataaccess.pet.Pet;
import com.myvet.dataaccess.vet.Vet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private Vet vet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "pet_name", nullable = false, length = 100)
    private String petName;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 5)
    private String time;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.scheduled;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
