package com.petvax.care.service;

import com.petvax.care.entity.RefreshToken;
import com.petvax.care.entity.User;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.repository.RefreshTokenRepository;
import com.petvax.care.repository.UserRepository;
import com.petvax.care.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@Transactional
public class AuthService {
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    private final Random random = new Random();
    private final java.util.Map<String, OtpData> otpStorage = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Authenticate user and generate tokens
     */
    public String[] authenticate(String usernameOrEmail, String password) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password", "INVALID_CREDENTIALS", 401));
        
        if (userService.isAccountLocked(user)) {
            throw new AuthenticationException("Account is locked. Try again after 15 minutes", "ACCOUNT_LOCKED", 429);
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            userService.recordFailedLogin(user);
            throw new AuthenticationException("Invalid username or password", "INVALID_CREDENTIALS", 401);
        }
        
        userService.resetFailedLogin(user);
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole().toString());
        String refreshToken = generateRefreshToken(user);
        
        log.info("User authenticated successfully: {}", user.getUsername());
        
        return new String[]{accessToken, refreshToken};
    }
    
    /**
     * Refresh access token
     */
    public String refreshAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenValueAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired refresh token", "INVALID_REFRESH_TOKEN", 401));
        
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new AuthenticationException("Refresh token has expired", "REFRESH_TOKEN_EXPIRED", 401);
        }
        
        User user = token.getUser();
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole().toString());
        
        log.info("Access token refreshed for user: {}", user.getUsername());
        
        return newAccessToken;
    }
    
    /**
     * Generate refresh token and save to database
     */
    private String generateRefreshToken(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            token.setRevoked(true);
        });
        
        String tokenValue = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenValue(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();
        
        refreshTokenRepository.save(refreshToken);
        
        return tokenValue;
    }
    
    /**
     * Generate and send OTP for email verification
     */
    public void sendVerificationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        String otp = generateOtp();
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        
        emailService.sendVerificationEmail(email, otp);
        log.info("Verification OTP sent to: {}", email);
    }
    
    /**
     * Verify email with OTP
     */
    public void verifyEmail(String email, String otp) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            throw new AuthenticationException("OTP not found or expired", "INVALID_OTP", 400);
        }
        
        if (LocalDateTime.now().isAfter(otpData.expiresAt)) {
            otpStorage.remove(email);
            throw new AuthenticationException("OTP has expired", "OTP_EXPIRED", 400);
        }
        
        if (!otpData.otp.equals(otp)) {
            throw new AuthenticationException("Invalid OTP", "INVALID_OTP", 400);
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        userService.verifyEmail(user.getId());
        otpStorage.remove(email);
        
        log.info("Email verified for: {}", email);
    }
    
    /**
     * Generate and send OTP for password reset
     */
    public void sendPasswordResetOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        String otp = generateOtp();
        String resetKey = "pwd_reset_" + email;
        otpStorage.put(resetKey, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        
        emailService.sendPasswordResetEmail(email, otp);
        log.info("Password reset OTP sent to: {}", email);
    }
    
    /**
     * Reset password with OTP
     */
    public void resetPassword(String email, String otp, String newPassword) {
        String resetKey = "pwd_reset_" + email;
        OtpData otpData = otpStorage.get(resetKey);
        
        if (otpData == null) {
            throw new AuthenticationException("OTP not found or expired", "INVALID_OTP", 400);
        }
        
        if (LocalDateTime.now().isAfter(otpData.expiresAt)) {
            otpStorage.remove(resetKey);
            throw new AuthenticationException("OTP has expired", "OTP_EXPIRED", 400);
        }
        
        if (!otpData.otp.equals(otp)) {
            throw new AuthenticationException("Invalid OTP", "INVALID_OTP", 400);
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        userService.updatePassword(user.getId(), newPassword);
        otpStorage.remove(resetKey);
        
        log.info("Password reset for: {}", email);
    }
    
    /**
     * Revoke refresh token (logout)
     */
    public void revokeRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
        
        Optional<RefreshToken> token = refreshTokenRepository.findByUser(user);
        if (token.isPresent()) {
            token.get().setRevoked(true);
            refreshTokenRepository.save(token.get());
        }
        
        log.info("Refresh token revoked for user: {}", user.getUsername());
    }
    
    /**
     * Generate random OTP
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Inner class for OTP data
     */
    private static class OtpData {
        String otp;
        LocalDateTime expiresAt;
        
        OtpData(String otp, LocalDateTime expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }
}
