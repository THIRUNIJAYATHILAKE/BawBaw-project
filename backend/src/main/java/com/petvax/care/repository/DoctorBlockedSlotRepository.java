package com.petvax.care.repository;

import com.petvax.care.entity.DoctorBlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoctorBlockedSlotRepository extends JpaRepository<DoctorBlockedSlot, Long> {

    List<DoctorBlockedSlot> findByDoctorId(Long doctorId);

    @Query("SELECT d FROM DoctorBlockedSlot d WHERE d.doctor.id = :doctorId AND d.blockedFrom <= :endTime AND d.blockedUntil >= :startTime")
    List<DoctorBlockedSlot> findConflictingSlots(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
