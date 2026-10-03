package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableDoctorDto {
    
    private Long doctorId;
    
    private String doctorName;
    
    private String specialization;
    
    private Integer yearsOfExperience;
    
    private String clinicName;
    
    private Integer availableSlots;
}
