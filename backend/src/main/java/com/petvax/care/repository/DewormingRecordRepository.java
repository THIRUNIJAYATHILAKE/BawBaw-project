package com.petvax.care.repository;

import com.petvax.care.entity.DewormingRecord;
import com.petvax.care.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DewormingRecordRepository extends JpaRepository<DewormingRecord, Long> {
    
    /**
     * Find all deworming records for a medical record
     */
    List<DewormingRecord> findByMedicalRecord(MedicalRecord medicalRecord);
    
    /**
     * Find deworming records due (nextDoseDate is today or earlier, excluding null)
     */
    @Query("SELECT d FROM DewormingRecord d WHERE d.nextDoseDate IS NOT NULL AND d.nextDoseDate <= :date")
    List<DewormingRecord> findDewormingRecordsDue(@Param("date") LocalDate date);
    
    /**
     * Find deworming records due for a specific medical record
     */
    @Query("SELECT d FROM DewormingRecord d WHERE d.medicalRecord = :medicalRecord AND d.nextDoseDate IS NOT NULL AND d.nextDoseDate <= :date")
    List<DewormingRecord> findDewormingRecordsDueByMedicalRecord(@Param("medicalRecord") MedicalRecord medicalRecord, @Param("date") LocalDate date);
}
