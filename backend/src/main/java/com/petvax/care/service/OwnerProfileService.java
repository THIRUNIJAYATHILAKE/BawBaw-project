package com.petvax.care.service;

import com.petvax.care.dto.OwnerProfileResponse;
import com.petvax.care.dto.PetResponse;
import com.petvax.care.dto.UpdateProfileRequest;
import com.petvax.care.entity.AccountStatus;
import com.petvax.care.entity.Pet;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.exception.UserNotFoundException;
import com.petvax.care.repository.PetRepository;
import com.petvax.care.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OwnerProfileService {
    
    private static final String PROFILE_PHOTOS_DIRECTORY = "uploads/profile-photos";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpeg", "jpg", "png"};
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private PetService petService;
    
    /**
     * Get owner's profile with pet count
     */
    @Transactional(readOnly = true)
    public OwnerProfileResponse getProfileByUser(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        List<Pet> pets = petRepository.findByOwner(user);
        
        return OwnerProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .profilePhotoUrl(null) // TODO: implement profile photo storage
                .petCount(pets.size())
                .accountStatus(user.getAccountStatus().toString())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * Update owner's profile information
     */
    public OwnerProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        
        if (request.getEmail() != null) {
            String newEmail = request.getEmail();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(newEmail);
        }
        
        user.setUpdatedBy(user.getUsername());
        user = userRepository.save(user);
        
        log.info("Profile updated successfully for user ID: {}", userId);
        
        List<Pet> pets = petRepository.findByOwner(user);
        return OwnerProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .profilePhotoUrl(null) // TODO: implement profile photo storage
                .petCount(pets.size())
                .accountStatus(user.getAccountStatus().toString())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * Upload or update profile photo
     */
    public String uploadProfilePhoto(Long userId, MultipartFile file) throws IOException {
        log.info("Uploading profile photo for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        validateFile(file);
        
        // Create directory if it doesn't exist
        Path uploadDir = Paths.get(PROFILE_PHOTOS_DIRECTORY);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("Created upload directory: {}", uploadDir);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String filename = "profile_" + userId + "_" + System.currentTimeMillis() + "." + fileExtension;
        
        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, file.getBytes());
        
        String photoUrl = PROFILE_PHOTOS_DIRECTORY + "/" + filename;
        
        log.info("Profile photo uploaded successfully for user ID: {} at path: {}", userId, photoUrl);
        return photoUrl;
    }
    
    /**
     * Soft delete account (set to INACTIVE and schedule for 90-day deletion)
     */
    public void deleteAccount(Long userId) {
        log.info("Soft deleting account for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        user.setAccountStatus(AccountStatus.INACTIVE);
        user.setSoftDeleteAt(LocalDateTime.now());
        user.setUpdatedBy(user.getUsername());
        
        userRepository.save(user);
        log.info("Account soft deleted successfully for user ID: {}. Will be permanently deleted in 90 days.", userId);
    }
    
    /**
     * Get all pets owned by the user
     */
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsList(Long userId) {
        log.info("Fetching pets list for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        
        List<Pet> pets = petRepository.findByOwner(user);
        
        return petService.getPetsByOwner(user);
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }
        
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        boolean isAllowed = false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (fileExtension.equals(allowed)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new IllegalArgumentException("Only JPEG and PNG files are allowed");
        }
    }
}
