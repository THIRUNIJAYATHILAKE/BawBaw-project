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
public class ReminderResponse {

    private Long id;
    private String reminderType;
    private String title;
    private String message;
    private LocalDateTime scheduledDate;
    private Boolean sent;
    private LocalDateTime sentAt;
    private Long medicalRecordId;
    private Long appointmentId;
    private String petName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
