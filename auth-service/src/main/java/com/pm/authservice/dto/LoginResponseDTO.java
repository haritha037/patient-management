package com.pm.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter // for jackson
public class LoginResponseDTO {
    private final String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
