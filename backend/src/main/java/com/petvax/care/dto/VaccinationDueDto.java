package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationDueDto {
    
    private Long id;
    
    private String vaccineName;
    
    private LocalDate nextBoosterDate;
    
    private String recordType;
    
    private boolean isDue;
}
