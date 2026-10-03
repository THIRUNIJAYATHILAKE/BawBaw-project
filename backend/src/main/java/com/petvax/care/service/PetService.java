package com.petvax.care.service;

import com.petvax.care.dto.*;
import com.petvax.care.entity.*;
import com.petvax.care.exception.PetNotFoundException;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PetService {
    
    private static final String PHOTOS_DIRECTORY = "uploads/pet-photos";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpeg", "jpg", "png"};
    
    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private VaccinationRepository vaccinationRepository;
    
    /**
     * Create a new pet for an owner
     */
    public PetResponse createPet(User owner, CreatePetRequest request) {
        log.info("Creating pet '{}' for owner ID: {}", request.getName(), owner.getId());
        
        Pet pet = Pet.builder()
                .owner(owner)
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .dateOfBirth(request.getDateOfBirth())
                .weight(request.getWeight())
                .sex(request.getSex())
                .markings(request.getMarkings())
                .microchipId(request.getMicrochipId())
                .notes(request.getNotes())
                .build();
        
        Pet savedPet = petRepository.save(pet);
        log.info("Pet created successfully with ID: {}", savedPet.getId());
        
        return mapToResponse(savedPet, new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Update pet details
     */
    public PetResponse updatePet(Long petId, User owner, CreatePetRequest request) {
        log.info("Updating pet ID: {} for owner ID: {}", petId, owner.getId());
        
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setDateOfBirth(request.getDateOfBirth());
        pet.setWeight(request.getWeight());
        pet.setSex(request.getSex());
        pet.setMarkings(request.getMarkings());
        pet.setMicrochipId(request.getMicrochipId());
        pet.setNotes(request.getNotes());
        
        Pet updatedPet = petRepository.save(pet);
        log.info("Pet updated successfully: {}", petId);
        
        return getPetById(petId, owner);
    }
    
    /**
     * Get a specific pet with appointments and vaccinations
     */
    public PetResponse getPetById(Long petId, User owner) {
        log.info("Fetching pet ID: {} for owner ID: {}", petId, owner.getId());
        
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        List<UpcomingAppointmentDto> appointments = getUpcomingAppointments(pet);
        List<VaccinationDueDto> vaccinations = getVaccinationsDue(pet);
        
        return mapToResponse(pet, appointments, vaccinations);
    }
    
    /**
     * Get all pets for an owner
     */
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsByOwner(User owner) {
        log.info("Fetching all pets for owner ID: {}", owner.getId());
        
        List<Pet> pets = petRepository.findByOwner(owner);
        
        return pets.stream()
                .map(pet -> {
                    List<UpcomingAppointmentDto> appointments = getUpcomingAppointments(pet);
                    List<VaccinationDueDto> vaccinations = getVaccinationsDue(pet);
                    return mapToResponse(pet, appointments, vaccinations);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a pet
     */
    public void deletePet(Long petId, User owner) {
        log.info("Deleting pet ID: {} for owner ID: {}", petId, owner.getId());
        
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        petRepository.delete(pet);
        log.info("Pet deleted successfully: {}", petId);
    }
    
    /**
     * Upload photo for a pet
     */
    public PetResponse uploadPetPhoto(Long petId, User owner, MultipartFile file) {
        log.info("Uploading photo for pet ID: {} by owner ID: {}", petId, owner.getId());
        
        Pet pet = petRepository.findByIdAndOwner(petId, owner)
                .orElseThrow(() -> new PetNotFoundException("Pet not found or you don't have access to this pet"));
        
        validatePhotoFile(file);
        
        String photoUrl = savePhotoFile(file, petId);
        pet.setPhotoUrl(photoUrl);
        
        Pet updatedPet = petRepository.save(pet);
        log.info("Photo uploaded successfully for pet ID: {}", petId);
        
        return getPetById(petId, owner);
    }
    
    /**
     * Get upcoming appointments for a pet
     */
    private List<UpcomingAppointmentDto> getUpcomingAppointments(Pet pet) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = appointmentRepository.findUpcomingAppointmentsByPet(pet, now);
        
        return appointments.stream()
                .map(apt -> UpcomingAppointmentDto.builder()
                        .id(apt.getId())
                        .appointmentDate(apt.getAppointmentDate())
                        .reason(apt.getReason())
                        .doctorName(apt.getDoctor() != null ? apt.getDoctor().getFullName() : "N/A")
                        .status(apt.getStatus().toString())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get vaccinations due for a pet
     */
    private List<VaccinationDueDto> getVaccinationsDue(Pet pet) {
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPet(pet);
        List<VaccinationDueDto> vaccinationsDue = new ArrayList<>();
        
        for (MedicalRecord record : medicalRecords) {
            List<Vaccination> vaccinations = vaccinationRepository.findByMedicalRecord(record);
            
            for (Vaccination vac : vaccinations) {
                if (vac.getNextBoosterDate() != null) {
                    boolean isDue = vac.getNextBoosterDate().isBefore(LocalDate.now()) || 
                                   vac.getNextBoosterDate().isEqual(LocalDate.now());
                    
                    vaccinationsDue.add(VaccinationDueDto.builder()
                            .id(vac.getId())
                            .vaccineName(vac.getVaccineName())
                            .nextBoosterDate(vac.getNextBoosterDate())
                            .recordType(record.getRecordType().toString())
                            .isDue(isDue)
                            .build());
                }
            }
        }
        
        return vaccinationsDue;
    }
    
    /**
     * Validate photo file
     */
    private void validatePhotoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        boolean isAllowed = Arrays.asList(ALLOWED_EXTENSIONS).contains(fileExtension);
        
        if (!isAllowed) {
            throw new IllegalArgumentException("Only JPEG and PNG formats are allowed");
        }
    }
    
    /**
     * Save photo file to disk
     */
    private String savePhotoFile(MultipartFile file, Long petId) {
        try {
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(PHOTOS_DIRECTORY);
            Files.createDirectories(uploadPath);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "pet_" + petId + "_" + System.currentTimeMillis() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, file.getBytes());
            
            // Return relative URL path
            return "uploads/pet-photos/" + filename;
        } catch (IOException e) {
            log.error("Error saving photo file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save photo file: " + e.getMessage());
        }
    }
    
    /**
     * Map Pet entity to PetResponse DTO
     */
    private PetResponse mapToResponse(Pet pet, List<UpcomingAppointmentDto> appointments, List<VaccinationDueDto> vaccinations) {
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
                .upcomingAppointments(appointments)
                .vaccinationsDue(vaccinations)
                .build();
    }
}
