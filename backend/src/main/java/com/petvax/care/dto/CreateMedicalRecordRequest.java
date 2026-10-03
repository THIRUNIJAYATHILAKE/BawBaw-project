package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMedicalRecordRequest {
    
    @NotBlank(message = "Record type is required")
    private String recordType;
    
    @NotNull(message = "Recorded date is required")
    private LocalDate recordedDate;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private LocalDate nextDueDate;
    
    private String notes;
}
