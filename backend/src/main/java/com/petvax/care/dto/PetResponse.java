package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponse {
    
    private Long id;
    
    private String name;
    
    private String species;
    
    private String breed;
    
    private LocalDate dateOfBirth;
    
    private BigDecimal weight;
    
    private String sex;
    
    private String markings;
    
    private String microchipId;
    
    private String photoUrl;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<UpcomingAppointmentDto> upcomingAppointments;
    
    private List<VaccinationDueDto> vaccinationsDue;
}
