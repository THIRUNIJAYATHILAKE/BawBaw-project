package com.petvax.care.service;

import com.petvax.care.dto.RegisterRequest;
import com.petvax.care.entity.AccountStatus;
import com.petvax.care.entity.User;
import com.petvax.care.entity.UserRole;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.repository.UserRepository;
import com.petvax.care.util.PasswordValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
@Transactional
public class UserService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Register a new user
     */
    public User registerUser(RegisterRequest request) {
        validateRegistrationRequest(request);
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already exists", "USERNAME_EXISTS", 409);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists", "EMAIL_EXISTS", 409);
        }
        
        if (!PasswordValidator.isValid(request.getPassword())) {
            String validationMsg = PasswordValidator.getValidationMessage(request.getPassword());
            throw new AuthenticationException(validationMsg, "WEAK_PASSWORD", 400);
        }
        
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid role specified", "INVALID_ROLE", 400);
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .accountStatus(getInitialAccountStatus(userRole))
                .emailVerified(false)
                .failedLoginAttempts(0)
                .createdBy(request.getUsername())
                .updatedBy(request.getUsername())
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully: {} ({})", user.getUsername(), user.getRole());
        
        emailService.sendAccountCreatedEmail(user.getEmail(), user.getUsername());
        
        return user;
    }
    
    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find user by username or email
     */
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Verify email
     */
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setEmailVerified(true);
        
        if (user.getRole() == UserRole.PET_OWNER) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        } else if (user.getRole() == UserRole.DOCTOR || user.getRole() == UserRole.ASSISTANT) {
            user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        }
        
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getUsername());
    }
    
    /**
     * Update password
     */
    public void updatePassword(Long userId, String newPassword) {
        if (!PasswordValidator.isValid(newPassword)) {
            String validationMsg = PasswordValidator.getValidationMessage(newPassword);
            throw new AuthenticationException(validationMsg, "WEAK_PASSWORD", 400);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(user.getUsername());
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getUsername());
    }
    
    /**
     * Update failed login attempts
     */
    public void recordFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= 5) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            log.warn("User account locked due to failed login attempts: {}", user.getUsername());
        }
        
        userRepository.save(user);
    }
    
    /**
     * Reset failed login attempts
     */
    public void resetFailedLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
    
    /**
     * Check if user account is locked
     */
    public boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        
        if (LocalDateTime.now().isBefore(user.getLockedUntil())) {
            return true;
        }
        
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        return false;
    }
    
    /**
     * Activate user account
     */
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("Account activated for user: {}", user.getUsername());
    }
    
    /**
     * Suspend user account
     */
    public void suspendAccount(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        log.warn("Account suspended for user: {} - Reason: {}", user.getUsername(), reason);
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate registration request
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AuthenticationException("Username is required", "INVALID_USERNAME", 400);
        }
        
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            throw new AuthenticationException("Username must be between 3 and 50 characters", "INVALID_USERNAME", 400);
        }
        
        if (!isValidEmail(request.getEmail())) {
            throw new AuthenticationException("Invalid email format", "INVALID_EMAIL", 400);
        }
        
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new AuthenticationException("Full name is required", "INVALID_FULL_NAME", 400);
        }
        
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new AuthenticationException("Role is required", "INVALID_ROLE", 400);
        }
    }
    
    /**
     * Get initial account status based on role
     */
    private AccountStatus getInitialAccountStatus(UserRole role) {
        return switch (role) {
            case PET_OWNER -> AccountStatus.PENDING_VERIFICATION;
            case DOCTOR, ASSISTANT -> AccountStatus.PENDING_VERIFICATION;
            case ADMIN -> AccountStatus.ACTIVE;
        };
    }
}
