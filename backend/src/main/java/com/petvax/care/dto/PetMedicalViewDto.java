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
public class PetMedicalViewDto {

    private Long id;

    private String name;

    private String species;

    private String breed;

    private LocalDate dateOfBirth;

    private BigDecimal weight;

    private String sex;

    private String microchipId;

    private String photoUrl;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Owner information
    private Long ownerId;

    private String ownerName;

    private String ownerEmail;

    private String ownerPhoneNumber;

    // Medical records
    private List<MedicalRecordDto> medicalRecords;

    // Appointments
    private List<AppointmentResponse> appointments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicalRecordDto {
        private Long id;

        private String recordType;

        private String doctorName;

        private LocalDate recordedDate;

        private String description;

        private LocalDate nextDueDate;

        private String notes;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
    }
}
