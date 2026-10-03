package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVaccinationRequest {
    
    @NotBlank(message = "Vaccine name is required")
    private String vaccineName;
    
    @NotNull(message = "Recorded date is required")
    private LocalDate recordedDate;
    
    private String batchNumber;
    
    @NotBlank(message = "Notes are required")
    private String notes;
}
