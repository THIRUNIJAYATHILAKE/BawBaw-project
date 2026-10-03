package com.petvax.care.controller;

import com.petvax.care.dto.CreatePetRequest;
import com.petvax.care.dto.PetResponse;
import com.petvax.care.entity.User;
import com.petvax.care.exception.PetNotFoundException;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.PetService;
import com.petvax.care.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/pets")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class PetController {
    
    @Autowired
    private PetService petService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new pet
     * POST /api/owner/pets
     */
    @PostMapping
    public ResponseEntity<?> createPet(@Valid @RequestBody CreatePetRequest request) {
        try {
            log.info("Creating new pet: {}", request.getName());
            
            User owner = getAuthenticatedUser();
            PetResponse pet = petService.createPet(owner, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pet created successfully");
            response.put("data", pet);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating pet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create pet"));
        }
    }
    
    /**
     * Get all pets for logged-in user
     * GET /api/owner/pets
     */
    @GetMapping
    public ResponseEntity<?> getAllPets() {
        try {
            log.info("Fetching all pets for user");
            
            User owner = getAuthenticatedUser();
            List<PetResponse> pets = petService.getPetsByOwner(owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pets retrieved successfully");
            response.put("data", pets);
            response.put("count", pets.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving pets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve pets"));
        }
    }
    
    /**
     * Get a specific pet with upcoming appointments and vaccination due dates
     * GET /api/owner/pets/{petId}
     */
    @GetMapping("/{petId}")
    public ResponseEntity<?> getPetById(@PathVariable Long petId) {
        try {
            log.info("Fetching pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            PetResponse pet = petService.getPetById(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pet retrieved successfully");
            response.put("data", pet);
            
            return ResponseEntity.ok(response);
        } catch (PetNotFoundException e) {
            log.warn("Pet not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "errorCode", e.getErrorCode()));
        } catch (Exception e) {
            log.error("Error retrieving pet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve pet"));
        }
    }
    
    /**
     * Update pet details
     * PUT /api/owner/pets/{petId}
     */
    @PutMapping("/{petId}")
    public ResponseEntity<?> updatePet(@PathVariable Long petId, 
                                       @Valid @RequestBody CreatePetRequest request) {
        try {
            log.info("Updating pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            PetResponse pet = petService.updatePet(petId, owner, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pet updated successfully");
            response.put("data", pet);
            
            return ResponseEntity.ok(response);
        } catch (PetNotFoundException e) {
            log.warn("Pet not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "errorCode", e.getErrorCode()));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating pet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update pet"));
        }
    }
    
    /**
     * Delete a pet
     * DELETE /api/owner/pets/{petId}
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<?> deletePet(@PathVariable Long petId) {
        try {
            log.info("Deleting pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            petService.deletePet(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pet deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (PetNotFoundException e) {
            log.warn("Pet not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "errorCode", e.getErrorCode()));
        } catch (Exception e) {
            log.error("Error deleting pet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete pet"));
        }
    }
    
    /**
     * Upload photo for a pet
     * POST /api/owner/pets/{petId}/upload-photo
     */
    @PostMapping("/{petId}/upload-photo")
    public ResponseEntity<?> uploadPetPhoto(@PathVariable Long petId,
                                            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading photo for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            PetResponse pet = petService.uploadPetPhoto(petId, owner, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Photo uploaded successfully");
            response.put("data", pet);
            
            return ResponseEntity.ok(response);
        } catch (PetNotFoundException e) {
            log.warn("Pet not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "errorCode", e.getErrorCode()));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading photo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload photo: " + e.getMessage()));
        }
    }
    
    /**
     * Get authenticated user from security context
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User not authenticated");
            throw new UnauthorizedException("User not authenticated");
        }
        
        PrincipalUser principal = (PrincipalUser) authentication.getPrincipal();
        User user = userService.getUserById(principal.getUserId());
        
        if (user == null) {
            log.warn("User not found for ID: {}", principal.getUserId());
            throw new UnauthorizedException("User not found");
        }
        
        return user;
    }
}
