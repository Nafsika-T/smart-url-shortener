package com.urlshortener.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min=3, max=20)
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min= 6, max= 40)
    private String password;
}
