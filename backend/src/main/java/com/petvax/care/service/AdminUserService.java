package com.petvax.care.service;

import com.petvax.care.dto.AddAssistantRequest;
import com.petvax.care.dto.AddDoctorRequest;
import com.petvax.care.dto.UserManagementResponse;
import com.petvax.care.entity.*;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.repository.DoctorProfileRepository;
import com.petvax.care.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AdminUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DoctorProfileRepository doctorProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Verify/Approve a doctor account and set to ACTIVE
     */
    public UserManagementResponse verifyDoctor(Long userId, String adminUsername) {
        User doctor = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new AuthenticationException("User is not a doctor", "INVALID_ROLE", 400);
        }
        
        doctor.setAccountStatus(AccountStatus.ACTIVE);
        doctor.setUpdatedBy(adminUsername);
        doctor.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(doctor);
        
        // Trigger notification
        try {
            emailService.sendVerificationEmail(doctor.getEmail(), "Account verified");
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", doctor.getEmail(), e.getMessage());
        }
        
        log.info("Doctor {} verified by admin {}", userId, adminUsername);
        return buildUserResponse(savedUser, "Doctor account verified successfully");
    }
    
    /**
     * Verify/Approve an assistant account and set to ACTIVE
     */
    public UserManagementResponse verifyAssistant(Long userId, String adminUsername) {
        User assistant = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        if (assistant.getRole() != UserRole.ASSISTANT) {
            throw new AuthenticationException("User is not an assistant", "INVALID_ROLE", 400);
        }
        
        assistant.setAccountStatus(AccountStatus.ACTIVE);
        assistant.setUpdatedBy(adminUsername);
        assistant.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(assistant);
        
        // Trigger notification
        try {
            emailService.sendVerificationEmail(assistant.getEmail(), "Account verified");
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", assistant.getEmail(), e.getMessage());
        }
        
        log.info("Assistant {} verified by admin {}", userId, adminUsername);
        return buildUserResponse(savedUser, "Assistant account verified successfully");
    }
    
    /**
     * Reject a pending doctor or assistant account
     */
    public UserManagementResponse rejectPendingUser(Long userId, String rejectReason, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        if (user.getRole() == UserRole.PET_OWNER) {
            throw new AuthenticationException("Cannot reject pet owner accounts", "INVALID_ROLE", 400);
        }
        
        user.setAccountStatus(AccountStatus.INACTIVE);
        user.setUpdatedBy(adminUsername);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // Trigger notification
        try {
            String emailBody = "Your account application has been rejected";
            if (rejectReason != null && !rejectReason.isEmpty()) {
                emailBody += ". Reason: " + rejectReason;
            }
            emailService.sendVerificationEmail(user.getEmail(), "rejected");
        } catch (Exception e) {
            log.warn("Failed to send rejection email to {}: {}", user.getEmail(), e.getMessage());
        }
        
        log.info("User {} rejected by admin {} with reason: {}", userId, adminUsername, rejectReason);
        return buildUserResponse(savedUser, "User account rejected successfully");
    }
    
    /**
     * Suspend a user account
     */
    public UserManagementResponse suspendUser(Long userId, String reason, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.setUpdatedBy(adminUsername);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        try {
            String emailBody = "Your account has been suspended";
            if (reason != null && !reason.isEmpty()) {
                emailBody += ". Reason: " + reason;
            }
            emailService.sendVerificationEmail(user.getEmail(), "suspended");
        } catch (Exception e) {
            log.warn("Failed to send suspension email to {}: {}", user.getEmail(), e.getMessage());
        }
        
        log.info("User {} suspended by admin {}", userId, adminUsername);
        return buildUserResponse(savedUser, "User account suspended successfully");
    }
    
    /**
     * Enable/Re-enable a suspended or inactive user
     */
    public UserManagementResponse enableUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(adminUsername);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), "Account Restored");
        } catch (Exception e) {
            log.warn("Failed to send enable email to {}: {}", user.getEmail(), e.getMessage());
        }
        
        log.info("User {} enabled by admin {}", userId, adminUsername);
        return buildUserResponse(savedUser, "User account enabled successfully");
    }
    
    /**
     * Soft delete a user (data retained, marked as inactive, can be permanently deleted after 90 days)
     */
    public UserManagementResponse deleteUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setAccountStatus(AccountStatus.INACTIVE);
        user.setSoftDeleteAt(LocalDateTime.now());
        user.setUpdatedBy(adminUsername);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), "Account Deleted");
        } catch (Exception e) {
            log.warn("Failed to send deletion email to {}: {}", user.getEmail(), e.getMessage());
        }
        
        log.info("User {} soft-deleted by admin {}", userId, adminUsername);
        return buildUserResponse(savedUser, "User account deleted successfully (soft delete with 90-day retention)");
    }
    
    /**
     * Add a new doctor account (admin-only)
     */
    public UserManagementResponse addNewDoctor(AddDoctorRequest request, String adminUsername) {
        validateDoctorRequest(request);
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already exists", "USERNAME_EXISTS", 409);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists", "EMAIL_EXISTS", 409);
        }
        
        User doctor = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(UserRole.DOCTOR)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .createdBy(adminUsername)
                .updatedBy(adminUsername)
                .build();
        
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            doctor.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        
        User savedUser = userRepository.save(doctor);
        
        // Create doctor profile
        DoctorProfile profile = DoctorProfile.builder()
                .user(savedUser)
                .licenseId(request.getLicenseId())
                .specialization(request.getSpecialization())
                .yearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0)
                .clinicName(request.getClinicName())
                .build();
        
        doctorProfileRepository.save(profile);
        
        try {
            emailService.sendAccountCreatedEmail(doctor.getEmail(), request.getUsername());
        } catch (Exception e) {
            log.warn("Failed to send account creation email to {}: {}", doctor.getEmail(), e.getMessage());
        }
        
        log.info("New doctor account created by admin {}: {}", adminUsername, request.getUsername());
        return buildUserResponse(savedUser, "Doctor account created successfully");
    }
    
    /**
     * Add a new assistant account (admin-only)
     */
    public UserManagementResponse addNewAssistant(AddAssistantRequest request, String adminUsername) {
        validateAssistantRequest(request);
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already exists", "USERNAME_EXISTS", 409);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists", "EMAIL_EXISTS", 409);
        }
        
        User assistant = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(UserRole.ASSISTANT)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .createdBy(adminUsername)
                .updatedBy(adminUsername)
                .build();
        
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            assistant.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        
        User savedUser = userRepository.save(assistant);
        
        try {
            emailService.sendAccountCreatedEmail(assistant.getEmail(), request.getUsername());
        } catch (Exception e) {
            log.warn("Failed to send account creation email to {}: {}", assistant.getEmail(), e.getMessage());
        }
        
        log.info("New assistant account created by admin {}: {}", adminUsername, request.getUsername());
        return buildUserResponse(savedUser, "Assistant account created successfully");
    }
    
    /**
     * Get all users with optional filters
     */
    public List<UserManagementResponse> getAllUsers(String role, String status, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, pageSize != null ? pageSize : 20);
        
        List<User> users;
        
        if (role != null && status != null) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            users = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == userRole && u.getAccountStatus() == accountStatus && u.getSoftDeleteAt() == null)
                    .collect(Collectors.toList());
        } else if (role != null) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            users = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == userRole && u.getSoftDeleteAt() == null)
                    .collect(Collectors.toList());
        } else if (status != null) {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            users = userRepository.findAll().stream()
                    .filter(u -> u.getAccountStatus() == accountStatus && u.getSoftDeleteAt() == null)
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll().stream()
                    .filter(u -> u.getSoftDeleteAt() == null)
                    .collect(Collectors.toList());
        }
        
        return users.stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all pending doctor and assistant verifications
     */
    public List<UserManagementResponse> getPendingVerifications() {
        List<User> pendingUsers = userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus() == AccountStatus.PENDING_VERIFICATION && u.getSoftDeleteAt() == null
                        && (u.getRole() == UserRole.DOCTOR || u.getRole() == UserRole.ASSISTANT))
                .collect(Collectors.toList());
        
        return pendingUsers.stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private void validateDoctorRequest(AddDoctorRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AuthenticationException("Username is required", "INVALID_REQUEST", 400);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AuthenticationException("Email is required", "INVALID_REQUEST", 400);
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new AuthenticationException("Full name is required", "INVALID_REQUEST", 400);
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new AuthenticationException("Password must be at least 8 characters", "INVALID_PASSWORD", 400);
        }
        if (request.getLicenseId() == null || request.getLicenseId().trim().isEmpty()) {
            throw new AuthenticationException("License ID is required", "INVALID_REQUEST", 400);
        }
    }
    
    private void validateAssistantRequest(AddAssistantRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AuthenticationException("Username is required", "INVALID_REQUEST", 400);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AuthenticationException("Email is required", "INVALID_REQUEST", 400);
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new AuthenticationException("Full name is required", "INVALID_REQUEST", 400);
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new AuthenticationException("Password must be at least 8 characters", "INVALID_PASSWORD", 400);
        }
    }
    
    private UserManagementResponse buildUserResponse(User user, String message) {
        return UserManagementResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .accountStatus(user.getAccountStatus().toString())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .message(message)
                .build();
    }
    
    private UserManagementResponse buildUserResponse(User user) {
        return buildUserResponse(user, null);
    }
}
