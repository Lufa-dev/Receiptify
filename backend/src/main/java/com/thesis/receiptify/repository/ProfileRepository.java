package com.thesis.receiptify.repository;

import com.thesis.receiptify.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile,Long>{
    Optional<Profile> findByUsername(String username);
    Optional<Profile> findByEmail(String email);

    Page<Profile> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);
}
