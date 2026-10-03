package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentFilterRequest {
    
    private String status;
    
    private String startDate;
    
    private String endDate;
    
    private Integer pageNumber = 0;
    
    private Integer pageSize = 20;
    
    private String sortBy = "appointmentDate";
    
    private String sortDirection = "ASC";
}
