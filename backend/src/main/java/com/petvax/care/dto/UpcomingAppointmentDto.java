package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingAppointmentDto {
    
    private Long id;
    
    private LocalDateTime appointmentDate;
    
    private String reason;
    
    private String doctorName;
    
    private String status;
}
