package com.myvet.dataaccess.vet;

import com.myvet.dataaccess.baseentity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vid;

    @Column(nullable = false, length = 75)
    private String firstName;

    @Column(nullable = false, length = 75)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String phone;

    // Vet-specific fields
    @Column(length = 50)
    private String licenseNumber;

    @Column(length = 100)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
