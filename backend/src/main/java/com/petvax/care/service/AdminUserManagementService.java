package com.petvax.care.service;

import com.petvax.care.dto.AddUserRequest;
import com.petvax.care.dto.UserManagementResponse;
import com.petvax.care.entity.*;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.exception.UserNotFoundException;
import com.petvax.care.repository.UserRepository;
import com.petvax.care.repository.DoctorProfileRepository;
import com.petvax.care.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminUserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all users with optional filtering
     */
    public List<UserManagementResponse> getAllUsers(String role, String status, int page, int size) {
        log.info("Fetching users - role: {}, status: {}", role, status);

        List<User> users;
        if (role != null && status != null) {
            users = userRepository.findAll(); // Apply filter in app for simplicity
            users = users.stream()
                    .filter(u -> !u.getAccountStatus().equals(AccountStatus.INACTIVE) || u.getSoftDeleted() == null
                            || !u.getSoftDeleted())
                    .filter(u -> role.isEmpty() || u.getRole().toString().equals(role))
                    .filter(u -> status.isEmpty() || u.getAccountStatus().toString().equals(status))
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll();
            users = users.stream()
                    .filter(u -> !u.getAccountStatus().equals(AccountStatus.INACTIVE) || u.getSoftDeleted() == null
                            || !u.getSoftDeleted())
                    .collect(Collectors.toList());
        }

        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending verifications (doctors and assistants)
     */
    public List<UserManagementResponse> getPendingVerifications() {
        log.info("Fetching pending verifications");

        return userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION))
                .filter(u -> u.getRole().equals(UserRole.DOCTOR) || u.getRole().equals(UserRole.ASSISTANT))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Verify doctor account
     */
    @Transactional
    public UserManagementResponse verifyDoctor(Long doctorId, String status, String reason, Long adminId) {
        log.info("Verifying doctor account: {}", doctorId);

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found"));

        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new AuthenticationException("User is not a doctor");
        }

        if ("APPROVED".equalsIgnoreCase(status)) {
            doctor.setAccountStatus(AccountStatus.ACTIVE);
            doctor.setVerifiedReason("Approved by admin");

            DoctorProfile profile = doctorProfileRepository.findByUserId(doctorId)
                    .orElseThrow(() -> new UserNotFoundException("Doctor profile not found"));
            profile.setVerifiedBy(userRepository.findById(adminId).orElse(null));
            profile.setVerifiedAt(LocalDateTime.now());
            doctorProfileRepository.save(profile);

            logAuditAction(adminId, "DOCTOR_VERIFIED", "users", doctorId, null, status);

            // Send notification
            notificationService.sendPushNotification(doctorId, "Account Approved",
                    "Your doctor account has been approved!", null);
        } else {
            doctor.setAccountStatus(AccountStatus.INACTIVE);
            doctor.setVerifiedReason(reason);
            logAuditAction(adminId, "DOCTOR_REJECTED", "users", doctorId, null, reason);
        }

        userRepository.save(doctor);
        return convertToResponse(doctor);
    }

    /**
     * Verify assistant account
     */
    @Transactional
    public UserManagementResponse verifyAssistant(Long assistantId, String status, String reason, Long adminId) {
        log.info("Verifying assistant account: {}", assistantId);

        User assistant = userRepository.findById(assistantId)
                .orElseThrow(() -> new UserNotFoundException("Assistant not found"));

        if (!assistant.getRole().equals(UserRole.ASSISTANT)) {
            throw new AuthenticationException("User is not an assistant");
        }

        if ("APPROVED".equalsIgnoreCase(status)) {
            assistant.setAccountStatus(AccountStatus.ACTIVE);
            assistant.setVerifiedReason("Approved by admin");
            logAuditAction(adminId, "ASSISTANT_VERIFIED", "users", assistantId, null, status);

            notificationService.sendPushNotification(assistantId, "Account Approved",
                    "Your assistant account has been approved!", null);
        } else {
            assistant.setAccountStatus(AccountStatus.INACTIVE);
            assistant.setVerifiedReason(reason);
            logAuditAction(adminId, "ASSISTANT_REJECTED", "users", assistantId, null, reason);
        }

        userRepository.save(assistant);
        return convertToResponse(assistant);
    }

    /**
     * Suspend user account
     */
    @Transactional
    public UserManagementResponse suspendUser(Long userId, String reason, Long adminId) {
        log.info("Suspending user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.setVerifiedReason(reason);
        userRepository.save(user);

        logAuditAction(adminId, "USER_SUSPENDED", "users", userId, null, reason);
        return convertToResponse(user);
    }

    /**
     * Enable suspended user account
     */
    @Transactional
    public UserManagementResponse enableUser(Long userId, Long adminId) {
        log.info("Enabling user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setVerifiedReason(null);
        userRepository.save(user);

        logAuditAction(adminId, "USER_ENABLED", "users", userId, null, null);
        return convertToResponse(user);
    }

    /**
     * Soft delete user (data retained for 90 days)
     */
    @Transactional
    public void deleteUser(Long userId, String reason, Long adminId) {
        log.info("Soft deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setSoftDeleted(true);
        user.setSoftDeletedAt(LocalDateTime.now());
        user.setSoftDeleteReason(reason);
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);

        logAuditAction(adminId, "USER_SOFT_DELETED", "users", userId, null, reason);
    }

    /**
     * Add new doctor account (admin only)
     */
    @Transactional
    public UserManagementResponse addDoctorAccount(AddUserRequest request, Long adminId) {
        log.info("Admin adding new doctor account: {}", request.getEmail());

        // Check if user exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthenticationException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthenticationException("Email already exists");
        }

        // Create user
        User doctor = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(UserRole.DOCTOR)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .createdBy("ADMIN_" + adminId)
                .updatedBy("ADMIN_" + adminId)
                .build();

        User savedDoctor = userRepository.save(doctor);

        // Create doctor profile
        DoctorProfile profile = DoctorProfile.builder()
                .user(savedDoctor)
                .licenseId(request.getLicenseId())
                .specialization(request.getSpecialization())
                .yearsOfExperience(request.getYearsOfExperience())
                .clinicName(request.getClinicName())
                .verifiedBy(userRepository.findById(adminId).orElse(null))
                .verifiedAt(LocalDateTime.now())
                .build();

        doctorProfileRepository.save(profile);

        logAuditAction(adminId, "DOCTOR_CREATED_BY_ADMIN", "users", savedDoctor.getId(), null, request.getFullName());

        return convertToResponse(savedDoctor);
    }

    /**
     * Add new assistant account (admin only)
     */
    @Transactional
    public UserManagementResponse addAssistantAccount(AddUserRequest request, Long adminId) {
        log.info("Admin adding new assistant account: {}", request.getEmail());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthenticationException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthenticationException("Email already exists");
        }

        User assistant = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(UserRole.ASSISTANT)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .createdBy("ADMIN_" + adminId)
                .updatedBy("ADMIN_" + adminId)
                .build();

        User savedAssistant = userRepository.save(assistant);
        logAuditAction(adminId, "ASSISTANT_CREATED_BY_ADMIN", "users", savedAssistant.getId(), null,
                request.getFullName());

        return convertToResponse(savedAssistant);
    }

    /**
     * Change user role
     */
    @Transactional
    public UserManagementResponse changeUserRole(Long userId, String newRole, Long adminId) {
        log.info("Changing user {} role to {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserRole oldRole = user.getRole();
        user.setRole(UserRole.valueOf(newRole));
        userRepository.save(user);

        logAuditAction(adminId, "USER_ROLE_CHANGED", "users", userId,
                oldRole.toString(), newRole);

        return convertToResponse(user);
    }

    // Helper methods

    private UserManagementResponse convertToResponse(User user) {
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
                .build();
    }

    private void logAuditAction(Long userId, String action, String entityType, Long entityId, String oldValue,
            String newValue) {
        AuditLog log = AuditLog.builder()
                .user(userRepository.findById(userId).orElse(null))
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValues(oldValue)
                .newValues(newValue)
                .build();
        auditLogRepository.save(log);
    }
}
