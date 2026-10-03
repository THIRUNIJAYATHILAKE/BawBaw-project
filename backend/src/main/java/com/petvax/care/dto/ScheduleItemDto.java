package com.petvax.care.dto;

import com.petvax.care.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItemDto {

    private Long appointmentId;

    private Long petId;

    private String petName;

    private String petSpecies;

    private Long ownerId;

    private String ownerName;

    private String ownerPhoneNumber;

    private LocalDateTime appointmentDate;

    private Integer durationMinutes;

    private AppointmentStatus status;

    private String appointmentPurpose;

    private String reason;

    private String notes;

    private String microchipId;
}
