package com.petvax.care.repository;

import com.petvax.care.entity.Vaccination;
import com.petvax.care.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    
    /**
     * Find all vaccinations for a medical record
     */
    List<Vaccination> findByMedicalRecord(MedicalRecord medicalRecord);
    
    /**
     * Find vaccinations due (nextBoosterDate is today or earlier, excluding null)
     */
    @Query("SELECT v FROM Vaccination v WHERE v.nextBoosterDate IS NOT NULL AND v.nextBoosterDate <= :date")
    List<Vaccination> findVaccinationsDue(@Param("date") LocalDate date);
    
    /**
     * Find vaccinations due for a specific medical record
     */
    @Query("SELECT v FROM Vaccination v WHERE v.medicalRecord = :medicalRecord AND v.nextBoosterDate IS NOT NULL AND v.nextBoosterDate <= :date")
    List<Vaccination> findVaccinationsDueByMedicalRecord(@Param("medicalRecord") MedicalRecord medicalRecord, @Param("date") LocalDate date);
}
