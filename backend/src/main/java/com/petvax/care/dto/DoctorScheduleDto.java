package com.petvax.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorScheduleDto {

    private Integer dayOfWeek;

    private String dayName;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isAvailable;

    private List<ScheduleItemDto> appointments;
}
