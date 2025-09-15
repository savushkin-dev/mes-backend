package com.host.SpringBootAutomationProduction.model;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password,

        String domain
) {
    public AuthRequest {
        if (domain == null || domain.isBlank()) {
            domain = "domain.local"; // default domain
        }
    }
}
