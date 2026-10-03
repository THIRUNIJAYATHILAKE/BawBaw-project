package com.petvax.care.repository;

import com.petvax.care.entity.DoctorProfile;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    /**
     * Find doctor profile by user
     */
    Optional<DoctorProfile> findByUser(User user);

    /**
     * Find doctor profile by user ID
     */
    Optional<DoctorProfile> findByUserId(Long userId);
}
