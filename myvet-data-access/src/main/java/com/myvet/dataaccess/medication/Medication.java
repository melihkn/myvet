package com.myvet.dataaccess.medication;

import com.myvet.dataaccess.enums.Enums.Zaman;
import com.myvet.dataaccess.enums.Enums.DrugType;
import com.myvet.dataaccess.enums.Enums.MedicalContext;
import com.myvet.dataaccess.baseentity.BaseEntity;
import com.myvet.dataaccess.pet.Pet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "cip_id", length = 50)
    private String cipId;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DrugType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicalContext context;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Zaman zaman;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;
}
