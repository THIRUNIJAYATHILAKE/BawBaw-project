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
public class MedicalRecordResponse {
    
    private Long id;
    
    private Long petId;
    
    private String recordType;
    
    private String description;
    
    private LocalDate recordedDate;
    
    private LocalDate nextDueDate;
    
    private String notes;
    
    private String doctorName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private VaccinationResponse vaccinationDetails;
    
    private DewormingResponse dewormingDetails;
}
