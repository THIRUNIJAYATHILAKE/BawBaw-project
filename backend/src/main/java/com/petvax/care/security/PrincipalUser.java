package com.petvax.care.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrincipalUser {
    private Long userId;
    private String username;
    private String role;
}
