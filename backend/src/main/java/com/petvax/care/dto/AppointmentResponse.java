package com.petvax.care.dto;

import com.petvax.care.entity.AppointmentStatus;
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
public class AppointmentResponse {
    
    private Long id;
    
    private Long petId;
    
    private String petName;
    
    private Long doctorId;
    
    private String doctorName;
    
    private Long ownerId;
    
    private String ownerName;
    
    private LocalDateTime appointmentDate;
    
    private Integer durationMinutes;
    
    private AppointmentStatus status;
    
    private AppointmentPurpose purpose;
    
    private String reason;
    
    private String notes;
    
    private LocalDateTime cancelledAt;
    
    private String cancelledReason;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
