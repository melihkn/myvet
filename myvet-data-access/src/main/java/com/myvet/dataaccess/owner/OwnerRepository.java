package com.myvet.dataaccess.owner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {
    Optional<Owner> findByEmail(String email);
    Optional<Owner> findByTc(Long tc);
    boolean existsByEmail(String email);
    boolean existsByTc(Long tc);
}
