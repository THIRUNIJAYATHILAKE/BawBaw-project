package com.petvax.care.service;

import com.petvax.care.dto.CreateMedicalRecordRequest;
import com.petvax.care.dto.MedicalRecordResponse;
import com.petvax.care.entity.*;
import com.petvax.care.exception.PetNotFoundException;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.repository.AuditLogRepository;
import com.petvax.care.repository.MedicalRecordRepository;
import com.petvax.care.repository.PetRepository;
import com.petvax.care.repository.UserRepository;
import com.petvax.care.repository.VaccinationRepository;
import com.petvax.care.repository.DewormingRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MedicalRecordService {
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VaccinationRepository vaccinationRepository;
    
    @Autowired
    private DewormingRecordRepository dewormingRecordRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get medical history for a pet (read-only for owners)
     */
    public List<MedicalRecordResponse> getMedicalHistory(Long petId, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        List<MedicalRecord> records = medicalRecordRepository.findByPet(pet);
        
        return records.stream()
                .sorted((r1, r2) -> r2.getRecordedDate().compareTo(r1.getRecordedDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get full medical history for a doctor
     */
    public List<MedicalRecordResponse> getMedicalHistoryForDoctor(Long petId, User doctor) {
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("Only doctors can access this endpoint");
        }
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found"));
        
        List<MedicalRecord> records = medicalRecordRepository.findByPet(pet);
        
        return records.stream()
                .sorted((r1, r2) -> r2.getRecordedDate().compareTo(r1.getRecordedDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new medical record (doctor only)
     */
    public MedicalRecordResponse createMedicalRecord(Long petId, CreateMedicalRecordRequest request, User doctor) {
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("Only doctors can create medical records");
        }
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found"));
        
        RecordType recordType = RecordType.valueOf(request.getRecordType().toUpperCase());
        
        MedicalRecord record = MedicalRecord.builder()
                .pet(pet)
                .recordType(recordType)
                .doctor(doctor)
                .recordedDate(request.getRecordedDate())
                .description(request.getDescription())
                .nextDueDate(request.getNextDueDate())
                .notes(request.getNotes())
                .build();
        
        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        
        // Audit log
        logAuditAction(doctor, "CREATE_MEDICAL_RECORD", "MedicalRecord", savedRecord.getId(), null, request);
        
        log.info("Medical record created for pet ID: {} by doctor ID: {}", petId, doctor.getId());
        
        return mapToResponse(savedRecord);
    }
    
    /**
     * Edit a medical record (doctor only, with audit logging)
     */
    public MedicalRecordResponse editMedicalRecord(Long petId, Long recordId, CreateMedicalRecordRequest request, User doctor) {
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("Only doctors can edit medical records");
        }
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found"));
        
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        
        if (!record.getPet().getId().equals(pet.getId())) {
            throw new UnauthorizedException("Medical record does not belong to this pet");
        }
        
        // Store old values for audit log
        String oldValues = serializeObject(record);
        
        RecordType recordType = RecordType.valueOf(request.getRecordType().toUpperCase());
        
        record.setRecordType(recordType);
        record.setRecordedDate(request.getRecordedDate());
        record.setDescription(request.getDescription());
        record.setNextDueDate(request.getNextDueDate());
        record.setNotes(request.getNotes());
        
        MedicalRecord updatedRecord = medicalRecordRepository.save(record);
        
        // Audit log
        String newValues = serializeObject(updatedRecord);
        logAuditActionWithValues(doctor, "UPDATE_MEDICAL_RECORD", "MedicalRecord", recordId, oldValues, newValues);
        
        log.info("Medical record ID: {} updated by doctor ID: {}", recordId, doctor.getId());
        
        return mapToResponse(updatedRecord);
    }
    
    /**
     * Get medical records by type for a pet
     */
    public List<MedicalRecordResponse> getMedicalRecordsByType(Long petId, String recordType, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        RecordType type = RecordType.valueOf(recordType.toUpperCase());
        List<MedicalRecord> records = medicalRecordRepository.findByPetAndRecordType(pet, type);
        
        return records.stream()
                .sorted((r1, r2) -> r2.getRecordedDate().compareTo(r1.getRecordedDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map MedicalRecord to MedicalRecordResponse
     */
    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        String doctorName = record.getDoctor() != null ? record.getDoctor().getFullName() : "N/A";
        
        MedicalRecordResponse response = MedicalRecordResponse.builder()
                .id(record.getId())
                .petId(record.getPet().getId())
                .recordType(record.getRecordType().name())
                .description(record.getDescription())
                .recordedDate(record.getRecordedDate())
                .nextDueDate(record.getNextDueDate())
                .notes(record.getNotes())
                .doctorName(doctorName)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
        
        // Attach vaccination details if it's a vaccination record
        if (record.getRecordType() == RecordType.VACCINATION) {
            List<Vaccination> vaccinations = vaccinationRepository.findByMedicalRecord(record);
            if (!vaccinations.isEmpty()) {
                Vaccination vac = vaccinations.get(0);
                response.setVaccinationDetails(mapVaccinationToResponse(vac));
            }
        }
        
        // Attach deworming details if it's a deworming record
        if (record.getRecordType() == RecordType.DEWORMING) {
            List<DewormingRecord> deworming = dewormingRecordRepository.findByMedicalRecord(record);
            if (!deworming.isEmpty()) {
                DewormingRecord dew = deworming.get(0);
                response.setDewormingDetails(mapDewormingToResponse(dew));
            }
        }
        
        return response;
    }
    
    private com.petvax.care.dto.VaccinationResponse mapVaccinationToResponse(Vaccination vac) {
        String administeredBy = vac.getAdministeredBy() != null ? vac.getAdministeredBy().getFullName() : "N/A";
        return com.petvax.care.dto.VaccinationResponse.builder()
                .id(vac.getId())
                .medicalRecordId(vac.getMedicalRecord().getId())
                .vaccineName(vac.getVaccineName())
                .batchNumber(vac.getBatchNumber())
                .administeredBy(administeredBy)
                .nextBoosterDate(vac.getNextBoosterDate())
                .createdAt(vac.getCreatedAt())
                .updatedAt(vac.getUpdatedAt())
                .build();
    }
    
    private com.petvax.care.dto.DewormingResponse mapDewormingToResponse(DewormingRecord dew) {
        String administeredBy = dew.getAdministeredBy() != null ? dew.getAdministeredBy().getFullName() : "N/A";
        return com.petvax.care.dto.DewormingResponse.builder()
                .id(dew.getId())
                .medicalRecordId(dew.getMedicalRecord().getId())
                .dewormerName(dew.getDewormerName())
                .doseMg(dew.getDoseMg())
                .administeredBy(administeredBy)
                .nextDoseDate(dew.getNextDoseDate())
                .createdAt(dew.getCreatedAt())
                .updatedAt(dew.getUpdatedAt())
                .build();
    }
    
    /**
     * Audit logging helper
     */
    private void logAuditAction(User user, String action, String entityType, Long entityId, String oldValues, Object newValues) {
        try {
            String newValuesStr = objectMapper.writeValueAsString(newValues);
            logAuditActionWithValues(user, action, entityType, entityId, oldValues, newValuesStr);
        } catch (Exception e) {
            log.error("Error serializing audit log values", e);
        }
    }
    
    private void logAuditActionWithValues(User user, String action, String entityType, Long entityId, String oldValues, String newValues) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }
    
    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error serializing object", e);
            return null;
        }
    }
}
