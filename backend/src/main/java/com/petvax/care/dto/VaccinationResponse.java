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
public class VaccinationResponse {
    
    private Long id;
    
    private Long medicalRecordId;
    
    private String vaccineName;
    
    private String batchNumber;
    
    private String administeredBy;
    
    private LocalDate nextBoosterDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
