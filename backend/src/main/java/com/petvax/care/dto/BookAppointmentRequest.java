package com.petvax.care.dto;

import com.petvax.care.entity.AppointmentPurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookAppointmentRequest {
    
    private Long petId;
    
    private Long doctorId;
    
    private LocalDateTime appointmentDate;
    
    private AppointmentPurpose purpose;
    
    private String reason;
}
