package com.host.SpringBootAutomationProduction.model;

import java.util.List;

public record AuthResponse(
        boolean success,
        String message,
        String username,
        String displayName,
        String email,
        List<String> groups
) {
    public static AuthResponse success(String message, String username,
                                       String displayName, String email,
                                       List<String> groups) {
        return new AuthResponse(true, message, username, displayName, email, groups);
    }

    public static AuthResponse error(String message, String username) {
        return new AuthResponse(false, message, username, null, null, null);
    }
}
