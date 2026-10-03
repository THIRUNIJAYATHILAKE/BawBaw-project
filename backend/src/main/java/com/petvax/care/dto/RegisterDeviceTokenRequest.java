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
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    private String deviceName;
    private String deviceType;
}
