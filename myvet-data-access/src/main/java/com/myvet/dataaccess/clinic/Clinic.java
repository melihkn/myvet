package com.myvet.dataaccess.clinic;

import com.myvet.dataaccess.baseentity.BaseEntity;
import com.myvet.dataaccess.vet.Vet;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clinic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clinic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer kid;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "working_hours_start", nullable = false, length = 5)
    private String workingHoursStart;

    @Column(name = "working_hours_end", nullable = false, length = 5)
    private String workingHoursEnd;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vid")
    private Vet responsibleVet;
}
