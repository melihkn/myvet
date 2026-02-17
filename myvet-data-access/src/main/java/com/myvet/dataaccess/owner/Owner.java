package com.myvet.dataaccess.owner;

import com.myvet.dataaccess.baseentity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Owner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @Column(unique = true)
    private Long tc;

    @Column(name = "vet_auth_uid", length = 128)
    private String vetAuthUid;

    // Owner-specific fields
    @Column(columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
}