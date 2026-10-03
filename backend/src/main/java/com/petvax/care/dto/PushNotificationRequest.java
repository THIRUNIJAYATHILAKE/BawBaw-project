package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotificationRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body/message is required")
    private String body;

    private String data;
    private Integer priority = 1;
}
