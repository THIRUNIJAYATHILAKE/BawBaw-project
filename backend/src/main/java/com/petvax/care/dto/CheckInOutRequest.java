package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInOutRequest {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    private String notes;
    
    private LocalDateTime timestamp;
}
