package com.petvax.care.service;

import com.petvax.care.dto.CreateDewormingRequest;
import com.petvax.care.dto.DewormingResponse;
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
public class DewormingService {
    
    @Autowired
    private DewormingRecordRepository dewormingRecordRepository;
    
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
     * Record deworming with auto-next-dose calculation
     * Schedule: Puppies: 2 weeks until 12 weeks; Adults: 3 months
     */
    public DewormingResponse recordDeworming(Long petId, CreateDewormingRequest request, User doctor) {
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("Only doctors can record deworming");
        }
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found"));
        
        // Calculate next dose date
        LocalDate nextDoseDate = calculateNextDoseDate(pet, request.getRecordedDate());
        
        // Create medical record for deworming
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .pet(pet)
                .recordType(RecordType.DEWORMING)
                .doctor(doctor)
                .recordedDate(request.getRecordedDate())
                .description("Deworming: " + request.getDewormerName())
                .nextDueDate(nextDoseDate)
                .notes(request.getNotes())
                .build();
        
        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        
        // Create deworming record
        DewormingRecord deworming = DewormingRecord.builder()
                .medicalRecord(savedMedicalRecord)
                .dewormerName(request.getDewormerName())
                .doseMg(request.getDoseMg())
                .administeredBy(doctor)
                .nextDoseDate(nextDoseDate)
                .build();
        
        DewormingRecord savedDeworming = dewormingRecordRepository.save(deworming);
        
        // Schedule reminders for the deworming
        reminderService.scheduledewormingReminders(savedDeworming);
        
        log.info("Deworming recorded for pet ID: {} by doctor ID: {}", petId, doctor.getId());
        
        return mapToResponse(savedDeworming);
    }
    
    /**
     * Calculate next dose date based on pet age
     * Puppies: every 2 weeks until 12 weeks old, then monthly until 6 months, then quarterly
     * Adults: every 3 months (quarterly)
     */
    public LocalDate calculateNextDoseDate(Pet pet, LocalDate currentDoseDate) {
        if (pet.getDateOfBirth() == null) {
            // Default to 3 months if age is unknown
            return currentDoseDate.plusMonths(3);
        }
        
        int ageInWeeks = VaccinationSchedule.getAgeInWeeks(pet.getDateOfBirth());
        int ageInMonths = VaccinationSchedule.getAgeInMonths(pet.getDateOfBirth());
        
        // Puppy: up to 12 weeks old
        if (ageInWeeks <= 12) {
            return currentDoseDate.plusWeeks(2);
        }
        
        // Juvenile: 12 weeks to 6 months
        if (ageInMonths <= 6) {
            return currentDoseDate.plusMonths(1);
        }
        
        // Adult: 6+ months
        return currentDoseDate.plusMonths(3);
    }
    
    /**
     * Get deworming history for a pet
     */
    public List<DewormingResponse> getDewormingByPet(Long petId, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPetAndRecordType(pet, RecordType.DEWORMING);
        
        List<DewormingResponse> deworming = new ArrayList<>();
        
        for (MedicalRecord record : medicalRecords) {
            List<DewormingRecord> recordDeworming = dewormingRecordRepository.findByMedicalRecord(record);
            deworming.addAll(recordDeworming.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }
        
        return deworming.stream()
                .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get due deworming records across all pets for an owner
     */
    public List<Map<String, Object>> getDueDewormingForOwner(User owner) {
        List<Pet> pets = petRepository.findByOwner(owner);
        List<DewormingRecord> dueDeworming = dewormingRecordRepository.findDewormingRecordsDue(LocalDate.now());
        
        return dueDeworming.stream()
                .filter(d -> pets.stream().anyMatch(p -> p.getId().equals(d.getMedicalRecord().getPet().getId())))
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", d.getId());
                    map.put("petId", d.getMedicalRecord().getPet().getId());
                    map.put("petName", d.getMedicalRecord().getPet().getName());
                    map.put("dewormerName", d.getDewormerName());
                    map.put("nextDoseDate", d.getNextDoseDate());
                    map.put("recordType", "DEWORMING");
                    map.put("isDue", d.getNextDoseDate() != null && d.getNextDoseDate().isBefore(LocalDate.now().plusDays(1)));
                    return map;
                })
                .sorted((d1, d2) -> {
                    LocalDate date1 = (LocalDate) d1.get("nextDoseDate");
                    LocalDate date2 = (LocalDate) d2.get("nextDoseDate");
                    if (date1 == null) return 1;
                    if (date2 == null) return -1;
                    return date1.compareTo(date2);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming deworming for a pet (within 30 days)
     */
    public List<DewormingResponse> getUpcomingDeworming(Long petId, User owner) {
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPetAndRecordType(pet, RecordType.DEWORMING);
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<DewormingResponse> upcomingDeworming = new ArrayList<>();
        
        for (MedicalRecord record : medicalRecords) {
            List<DewormingRecord> recordDeworming = dewormingRecordRepository.findByMedicalRecord(record);
            upcomingDeworming.addAll(recordDeworming.stream()
                    .filter(d -> d.getNextDoseDate() != null && 
                            !d.getNextDoseDate().isBefore(today) && 
                            !d.getNextDoseDate().isAfter(thirtyDaysFromNow))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }
        
        return upcomingDeworming.stream()
                .sorted((d1, d2) -> d1.getNextDoseDate().compareTo(d2.getNextDoseDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Map DewormingRecord to DewormingResponse
     */
    private DewormingResponse mapToResponse(DewormingRecord deworming) {
        String administeredBy = deworming.getAdministeredBy() != null ? deworming.getAdministeredBy().getFullName() : "N/A";
        return DewormingResponse.builder()
                .id(deworming.getId())
                .medicalRecordId(deworming.getMedicalRecord().getId())
                .dewormerName(deworming.getDewormerName())
                .doseMg(deworming.getDoseMg())
                .administeredBy(administeredBy)
                .nextDoseDate(deworming.getNextDoseDate())
                .createdAt(deworming.getCreatedAt())
                .updatedAt(deworming.getUpdatedAt())
                .build();
    }
}
