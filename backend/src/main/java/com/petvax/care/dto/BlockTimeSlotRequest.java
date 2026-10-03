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
public class BlockTimeSlotRequest {
    private Long doctorId;
    private LocalDateTime blockedFrom;
    private LocalDateTime blockedUntil;
    private String reason;
}
