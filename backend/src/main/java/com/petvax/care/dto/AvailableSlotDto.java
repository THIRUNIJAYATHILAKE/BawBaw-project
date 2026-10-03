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
public class AvailableSlotDto {
    
    private LocalDateTime slotTime;
    
    private Integer durationMinutes;
    
    private Boolean isAvailable;
}
