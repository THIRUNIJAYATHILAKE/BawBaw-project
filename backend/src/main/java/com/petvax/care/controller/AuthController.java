package com.petvax.care.controller;

import com.petvax.care.dto.*;
import com.petvax.care.entity.User;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.security.JwtTokenProvider;
import com.petvax.care.service.AuthService;
import com.petvax.care.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Register request for username: {}", request.getUsername());
            
            User user = userService.registerUser(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully. Please verify your email.");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().toString());
            
            authService.sendVerificationOtp(user.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Registration failed", "REGISTRATION_ERROR"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login request for user: {}", request.getUsername());
            
            String[] tokens = authService.authenticate(request.getUsername(), request.getPassword());
            
            String accessToken = tokens[0];
            String refreshToken = tokens[1];
            
            String username = jwtTokenProvider.extractUsername(accessToken);
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
            
            LoginResponse response = LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().toString())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Login failed", "LOGIN_ERROR"));
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.info("Refresh token request");
            
            String newAccessToken = authService.refreshAccessToken(request.getRefreshToken());
            
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Token refresh failed", "TOKEN_REFRESH_ERROR"));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            log.info("Email verification request for: {}", request.getEmail());
            
            authService.verifyEmail(request.getEmail(), request.getOtp());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            response.put("email", request.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Email verification failed", "EMAIL_VERIFICATION_ERROR"));
        }
    }
    
    @PostMapping("/send-verification-otp")
    public ResponseEntity<?> sendVerificationOtp(@RequestBody EmailVerificationRequest request) {
        try {
            log.info("Send verification OTP request for: {}", request.getEmail());
            
            authService.sendVerificationOtp(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Verification OTP sent to your email");
            response.put("email", request.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Send verification OTP error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to send verification OTP", "SEND_OTP_ERROR"));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Forgot password request for: {}", request.getEmail());
            
            authService.sendPasswordResetOtp(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset OTP sent to your email");
            response.put("email", request.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Forgot password error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to send password reset email", "FORGOT_PASSWORD_ERROR"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            log.info("Password reset request for: {}", request.getEmail());
            
            authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            response.put("email", request.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Password reset error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Password reset failed", "RESET_PASSWORD_ERROR"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                log.info("Logout request for user: {}", username);
                
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new AuthenticationException("User not found", "USER_NOT_FOUND", 404));
                
                authService.revokeRefreshToken(user.getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Logout failed", "LOGOUT_ERROR"));
        }
    }
    
    private ResponseEntity<?> handleAuthException(AuthenticationException e) {
        log.warn("Authentication error: {} - {}", e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getStatusCode())
                .body(createErrorResponse(e.getMessage(), e.getErrorCode()));
    }
    
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("errorCode", errorCode);
        return error;
    }
}
