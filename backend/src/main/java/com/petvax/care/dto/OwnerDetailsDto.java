package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDetailsDto {

    private Long id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String address;

    private LocalDate dateOfBirth;

    private String username;
}
