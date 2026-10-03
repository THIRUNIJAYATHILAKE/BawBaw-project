package com.petvax.care.service;

import com.petvax.care.dto.CreateVaccinationRequest;
import com.petvax.care.dto.VaccinationResponse;
import com.petvax.care.dto.VaccinationDueDto;
import com.petvax.care.entity.*;
import com.petvax.care.exception.PetNotFoundException;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.repository.*;
import com.petvax.care.util.VaccinationSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class VaccinationService {
    
    @Autowired
    private VaccinationRepository vaccinationRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ReminderService reminderService;
    
    /**
     * Record a vaccination with automatic next booster calculation
     */
    public VaccinationResponse recordVaccination(Long petId, CreateVaccinationRequest request, User doctor) {
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("Only doctors can record vaccinations");
        }
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found"));
        
        // Create medical record for vaccination
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .pet(pet)
                .recordType(RecordType.VACCINATION)
                .doctor(doctor)
                .recordedDate(request.getRecordedDate())
                .description("Vaccination: " + request.getVaccineName())
                .nextDueDate(calculateNextBoosterDate(request.getVaccineName(), request.getRecordedDate()))
                .notes(request.getNotes())
                .build();
        
        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        
        // Calculate next booster date
        LocalDate nextBoosterDate = calculateNextBoosterDate(request.getVaccineName(), request.getRecordedDate());
        
        // Create vaccination record
        Vaccination vaccination = Vaccination.builder()
                .medicalRecord(savedMedicalRecord)
                .vaccineName(request.getVaccineName())
                .batchNumber(request.getBatchNumber())
                .administeredBy(doctor)
                .nextBoosterDate(nextBoosterDate)
                .build();
        
        Vaccination savedVaccination = vaccinationRepository.save(vaccination);
        
        // Schedule reminders for the vaccination
        reminderService.scheduleVaccinationReminders(savedVaccination);
        
        log.info("Vaccination recorded for pet ID: {} by doctor ID: {}", petId, doctor.getId());
        
        return mapToResponse(savedVaccination);
    }
    
    /**
     * Get vaccination schedule (WSAVA guidelines)
     */
    public Map<String, Object> getVaccinationSchedule(Long petId, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        Map<String, Object> schedule = new HashMap<>();
        
        List<VaccinationSchedule.VaccinationScheduleItem> scheduleItems = new ArrayList<>();
        
        // Determine if puppy or kitten based on species and age
        if (pet.getDateOfBirth() != null) {
            if ("Dog".equalsIgnoreCase(pet.getSpecies())) {
                scheduleItems = VaccinationSchedule.getPuppyVaccinationSchedule();
            } else if ("Cat".equalsIgnoreCase(pet.getSpecies())) {
                scheduleItems = VaccinationSchedule.getKittenVaccinationSchedule();
            }
            
            int ageInWeeks = VaccinationSchedule.getAgeInWeeks(pet.getDateOfBirth());
            schedule.put("petAge", ageInWeeks + " weeks");
            schedule.put("petAgeInMonths", VaccinationSchedule.getAgeInMonths(pet.getDateOfBirth()) + " months");
        }
        
        schedule.put("species", pet.getSpecies());
        schedule.put("breed", pet.getBreed());
        schedule.put("recommendedSchedule", scheduleItems.stream()
                .map(item -> Map.of(
                        "age", item.getAgeDescription(),
                        "vaccine", item.getVaccineType().getDisplayName()
                ))
                .collect(Collectors.toList()));
        
        return schedule;
    }
    
    /**
     * Calculate next booster date based on vaccine type
     */
    public LocalDate calculateNextBoosterDate(String vaccineName, LocalDate administrationDate) {
        // Try to match vaccine name with predefined types
        for (VaccinationSchedule.VaccineType type : VaccinationSchedule.VaccineType.values()) {
            if (vaccineName.toUpperCase().contains(type.name())) {
                return VaccinationSchedule.calculateNextBoosterDate(type, administrationDate);
            }
        }
        // Default to 1 year if not matched
        return VaccinationSchedule.calculateNextBoosterDate(administrationDate, 365);
    }
    
    /**
     * Get all vaccinations for a pet
     */
    public List<VaccinationResponse> getVaccinationsByPet(Long petId, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPetAndRecordType(pet, RecordType.VACCINATION);
        
        List<VaccinationResponse> vaccinations = new ArrayList<>();
        
        for (MedicalRecord record : medicalRecords) {
            List<Vaccination> recordVaccinations = vaccinationRepository.findByMedicalRecord(record);
            vaccinations.addAll(recordVaccinations.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }
        
        return vaccinations.stream()
                .sorted((v1, v2) -> v2.getCreatedAt().compareTo(v1.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get due vaccinations across all pets for an owner
     */
    public List<VaccinationDueDto> getDueVaccinationsForOwner(User owner) {
        List<Pet> pets = petRepository.findByOwner(owner);
        List<Vaccination> dueVaccinations = vaccinationRepository.findVaccinationsDue(LocalDate.now());
        
        return dueVaccinations.stream()
                .filter(v -> pets.stream().anyMatch(p -> p.getId().equals(v.getMedicalRecord().getPet().getId())))
                .map(v -> VaccinationDueDto.builder()
                        .id(v.getId())
                        .vaccineName(v.getVaccineName())
                        .nextBoosterDate(v.getNextBoosterDate())
                        .recordType("VACCINATION")
                        .isDue(v.getNextBoosterDate() != null && v.getNextBoosterDate().isBefore(LocalDate.now().plusDays(1)))
                        .build())
                .sorted((v1, v2) -> {
                    if (v1.getNextBoosterDate() == null) return 1;
                    if (v2.getNextBoosterDate() == null) return -1;
                    return v1.getNextBoosterDate().compareTo(v2.getNextBoosterDate());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming vaccinations for a pet (within 30 days)
     */
    public List<VaccinationResponse> getUpcomingVaccinations(Long petId, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPetAndRecordType(pet, RecordType.VACCINATION);
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<VaccinationResponse> upcomingVaccinations = new ArrayList<>();
        
        for (MedicalRecord record : medicalRecords) {
            List<Vaccination> recordVaccinations = vaccinationRepository.findByMedicalRecord(record);
            upcomingVaccinations.addAll(recordVaccinations.stream()
                    .filter(v -> v.getNextBoosterDate() != null && 
                            !v.getNextBoosterDate().isBefore(today) && 
                            !v.getNextBoosterDate().isAfter(thirtyDaysFromNow))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }
        
        return upcomingVaccinations.stream()
                .sorted((v1, v2) -> v1.getNextBoosterDate().compareTo(v2.getNextBoosterDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Map Vaccination to VaccinationResponse
     */
    private VaccinationResponse mapToResponse(Vaccination vaccination) {
        String administeredBy = vaccination.getAdministeredBy() != null ? vaccination.getAdministeredBy().getFullName() : "N/A";
        return VaccinationResponse.builder()
                .id(vaccination.getId())
                .medicalRecordId(vaccination.getMedicalRecord().getId())
                .vaccineName(vaccination.getVaccineName())
                .batchNumber(vaccination.getBatchNumber())
                .administeredBy(administeredBy)
                .nextBoosterDate(vaccination.getNextBoosterDate())
                .createdAt(vaccination.getCreatedAt())
                .updatedAt(vaccination.getUpdatedAt())
                .build();
    }
}
