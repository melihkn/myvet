package com.myvet.dataaccess.operation;

import com.myvet.dataaccess.baseentity.BaseEntity;
import com.myvet.dataaccess.enums.Enums.MedicalContext;
import com.myvet.dataaccess.enums.Enums.OperationType;
import com.myvet.dataaccess.pet.Pet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "operation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OperationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicalContext context;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 500)
    private String outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
