package com.bondtradex.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestValidationRequest(
        @NotBlank(message = "Username is required")
        @Size(min=3,max=50,message = "user name should between 3 and 50")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
