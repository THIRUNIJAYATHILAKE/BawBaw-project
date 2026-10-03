package com.petvax.care.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisRecordRequest {

    @NotBlank(message = "Record type is required")
    private String recordType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Recorded date is required")
    private LocalDate recordedDate;

    private LocalDate nextDueDate;

    private String notes;
}
