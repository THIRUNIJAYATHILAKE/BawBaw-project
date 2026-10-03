package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DewormingResponse {
    
    private Long id;
    
    private Long medicalRecordId;
    
    private String dewormerName;
    
    private BigDecimal doseMg;
    
    private String administeredBy;
    
    private LocalDate nextDoseDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
