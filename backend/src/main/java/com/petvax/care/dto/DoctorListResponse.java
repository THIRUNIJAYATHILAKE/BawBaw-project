package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorListResponse {
    
    private Long doctorId;
    
    private String doctorName;
    
    private String specialization;
    
    private Integer yearsOfExperience;
    
    private String clinicName;
    
    private String licenseId;
    
    private Integer availableSlots;
    
    private Boolean isVerified;
    
    private LocalDateTime verifiedAt;
    
    private List<DoctorScheduleDto> schedule;
}
