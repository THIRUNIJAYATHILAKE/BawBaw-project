package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerProfileResponse {
    
    private Long userId;
    
    private String username;
    
    private String fullName;
    
    private String email;
    
    private String phoneNumber;
    
    private String address;
    
    private LocalDate dateOfBirth;
    
    private String profilePhotoUrl;
    
    private Integer petCount;
    
    private String accountStatus;
    
    private Boolean emailVerified;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
