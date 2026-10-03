package com.petvax.care.service;

import com.petvax.care.dto.*;
import com.petvax.care.entity.*;
import com.petvax.care.exception.PetNotFoundException;
import com.petvax.care.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DoctorSearchService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Search pets by name, owner name, microchip ID, or pet ID
     */
    @Transactional(readOnly = true)
    public List<PetResponse> searchPets(String query) {
        log.info("Searching pets with query: {}", query);
        
        List<Pet> results = new ArrayList<>();
        
        // Try to parse as Long for pet ID search
        try {
            Long petId = Long.parseLong(query);
            petRepository.findById(petId).ifPresent(results::add);
        } catch (NumberFormatException e) {
            // Not a number, continue with text search
        }
        
        // If results empty, do multi-field search
        if (results.isEmpty()) {
            results = petRepository.searchByNameOrOwnerNameOrMicrochipId(query);
        }
        
        return results.stream()
                .map(this::mapPetToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get full pet medical view with owner and medical records
     */
    @Transactional(readOnly = true)
    public PetMedicalViewDto getPetMedicalView(Long petId) {
        log.info("Fetching medical view for pet ID: {}", petId);
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + petId));
        
        PetMedicalViewDto dto = PetMedicalViewDto.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .dateOfBirth(pet.getDateOfBirth())
                .weight(pet.getWeight())
                .sex(pet.getSex())
                .microchipId(pet.getMicrochipId())
                .photoUrl(pet.getPhotoUrl())
                .notes(pet.getNotes())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .ownerId(pet.getOwner().getId())
                .ownerName(pet.getOwner().getFullName())
                .ownerEmail(pet.getOwner().getEmail())
                .ownerPhoneNumber(pet.getOwner().getPhoneNumber())
                .build();
        
        // Add medical records
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPetOrderByRecordedDateDesc(pet);
        List<PetMedicalViewDto.MedicalRecordDto> medicalRecordDtos = medicalRecords.stream()
                .map(mr -> PetMedicalViewDto.MedicalRecordDto.builder()
                        .id(mr.getId())
                        .recordType(mr.getRecordType().toString())
                        .doctorName(mr.getDoctor() != null ? mr.getDoctor().getFullName() : "N/A")
                        .recordedDate(mr.getRecordedDate())
                        .description(mr.getDescription())
                        .nextDueDate(mr.getNextDueDate())
                        .notes(mr.getNotes())
                        .createdAt(mr.getCreatedAt())
                        .updatedAt(mr.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        dto.setMedicalRecords(medicalRecordDtos);
        
        // Add appointments
        List<Appointment> appointments = appointmentRepository.findByPet(pet);
        List<AppointmentResponse> appointmentResponses = appointments.stream()
                .map(this::mapAppointmentToResponse)
                .collect(Collectors.toList());
        dto.setAppointments(appointmentResponses);
        
        return dto;
    }

    /**
     * Get owner details from pet's owner
     */
    @Transactional(readOnly = true)
    public OwnerDetailsDto getOwnerDetails(Long petId) {
        log.info("Fetching owner details for pet ID: {}", petId);
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + petId));
        
        User owner = pet.getOwner();
        
        return OwnerDetailsDto.builder()
                .id(owner.getId())
                .fullName(owner.getFullName())
                .email(owner.getEmail())
                .phoneNumber(owner.getPhoneNumber())
                .address(owner.getAddress())
                .dateOfBirth(owner.getDateOfBirth())
                .username(owner.getUsername())
                .build();
    }

    /**
     * Record diagnosis/medical record with audit logging
     */
    public MedicalRecord recordDiagnosis(Long petId, User doctor, DiagnosisRecordRequest request) {
        log.info("Recording diagnosis for pet ID: {} by doctor ID: {}", petId, doctor.getId());
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + petId));
        
        RecordType recordType = RecordType.valueOf(request.getRecordType().toUpperCase());
        
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .pet(pet)
                .recordType(recordType)
                .doctor(doctor)
                .recordedDate(request.getRecordedDate())
                .description(request.getDescription())
                .nextDueDate(request.getNextDueDate())
                .notes(request.getNotes())
                .build();
        
        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);
        log.info("Medical record created with ID: {}", savedRecord.getId());
        
        // Create audit log entry
        createAuditLog(doctor, "RECORD_DIAGNOSIS", "MedicalRecord", savedRecord.getId(), null, serializeObject(medicalRecord));
        
        return savedRecord;
    }

    /**
     * Edit medical record with audit logging
     */
    public MedicalRecord editMedicalRecord(Long medicalRecordId, User doctor, DiagnosisRecordRequest request) {
        log.info("Editing medical record ID: {} by doctor ID: {}", medicalRecordId, doctor.getId());
        
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found with ID: " + medicalRecordId));
        
        // Store old values for audit
        String oldValues = serializeObject(medicalRecord);
        
        RecordType recordType = RecordType.valueOf(request.getRecordType().toUpperCase());
        
        medicalRecord.setRecordType(recordType);
        medicalRecord.setDescription(request.getDescription());
        medicalRecord.setRecordedDate(request.getRecordedDate());
        medicalRecord.setNextDueDate(request.getNextDueDate());
        medicalRecord.setNotes(request.getNotes());
        
        MedicalRecord updatedRecord = medicalRecordRepository.save(medicalRecord);
        log.info("Medical record updated: {}", medicalRecordId);
        
        // Create audit log entry
        createAuditLog(doctor, "EDIT_MEDICAL_RECORD", "MedicalRecord", updatedRecord.getId(), oldValues, serializeObject(updatedRecord));
        
        return updatedRecord;
    }

    /**
     * Create audit log entry
     */
    private void createAuditLog(User user, String action, String entityType, Long entityId, String oldValues, String newValues) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress("N/A")
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for action: {}", action);
        } catch (Exception e) {
            log.error("Error creating audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Serialize object to JSON string
     */
    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error serializing object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Map Pet to PetResponse
     */
    private PetResponse mapPetToResponse(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .dateOfBirth(pet.getDateOfBirth())
                .weight(pet.getWeight())
                .sex(pet.getSex())
                .markings(pet.getMarkings())
                .microchipId(pet.getMicrochipId())
                .photoUrl(pet.getPhotoUrl())
                .notes(pet.getNotes())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .build();
    }

    /**
     * Map Appointment to AppointmentResponse
     */
    private AppointmentResponse mapAppointmentToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .petId(appointment.getPet().getId())
                .petName(appointment.getPet().getName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .ownerId(appointment.getOwner().getId())
                .ownerName(appointment.getOwner().getFullName())
                .appointmentDate(appointment.getAppointmentDate())
                .durationMinutes(appointment.getDurationMinutes())
                .status(appointment.getStatus())
                .purpose(appointment.getPurpose())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .cancelledAt(appointment.getCancelledAt())
                .cancelledReason(appointment.getCancelledReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
