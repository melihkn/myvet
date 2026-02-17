package com.myvet.dataaccess.vet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetRepository extends JpaRepository<Vet, Integer> {
    Optional<Vet> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
}
