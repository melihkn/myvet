// myvet-data-access/src/main/java/com/myvet/dataaccess/repository/UserRepository.java
package com.myvet.dataaccess.repository;

import com.myvet.dataaccess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}