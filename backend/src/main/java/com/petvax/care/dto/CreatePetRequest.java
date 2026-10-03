package com.petvax.care.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePetRequest {
    
    @NotBlank(message = "Pet name is required")
    private String name;
    
    @NotBlank(message = "Species is required")
    private String species;
    
    private String breed;
    
    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;
    
    @DecimalMin(value = "0.1", message = "Weight must be greater than 0")
    private BigDecimal weight;
    
    private String sex;
    
    private String markings;
    
    private String microchipId;
    
    private String notes;
}
