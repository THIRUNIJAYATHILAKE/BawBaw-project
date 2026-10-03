package com.petvax.care.repository;

import com.petvax.care.entity.MedicalRecord;
import com.petvax.care.entity.Pet;
import com.petvax.care.entity.RecordType;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    /**
     * Find medical records for a pet
     */
    List<MedicalRecord> findByPet(Pet pet);
    
    /**
     * Find medical records for a pet by type
     */
    List<MedicalRecord> findByPetAndRecordType(Pet pet, RecordType recordType);
    
    /**
     * Find all medical records for a pet ordered by date
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.pet = :pet ORDER BY mr.recordedDate DESC")
    List<MedicalRecord> findByPetOrderByRecordedDateDesc(@Param("pet") Pet pet);
    
    /**
     * Find medical records by doctor
     */
    List<MedicalRecord> findByDoctor(User doctor);
}
