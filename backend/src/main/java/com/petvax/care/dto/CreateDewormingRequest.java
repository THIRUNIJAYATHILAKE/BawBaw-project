package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDewormingRequest {
    
    @NotBlank(message = "Dewormer name is required")
    private String dewormerName;
    
    @NotNull(message = "Recorded date is required")
    private LocalDate recordedDate;
    
    @DecimalMin(value = "0.1", message = "Dose must be greater than 0")
    private BigDecimal doseMg;
    
    @NotBlank(message = "Notes are required")
    private String notes;
}
