package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.model.AuthRequest;
import com.host.SpringBootAutomationProduction.model.AuthResponse;
import com.host.SpringBootAutomationProduction.service.DomainAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DomainAuthService authService;

//    @PostMapping("/test")
//    public ResponseEntity<AuthResponse> testAuthentication(@Valid @RequestBody AuthRequest request) {
//
//        AuthResponse response = authService.authenticate(
//                request.username(),
//                request.password(),
//                request.domain()
//        );
//
//        return ResponseEntity.ok(response);
//    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String result = authService.testConnection();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/config")
    public ResponseEntity<String> showConfig() {
        return ResponseEntity.ok("""
            Current configuration:
            - Use POST /api/auth/test with JSON body
            - Example: {"username": "youruser", "password": "yourpass", "domain": "yourdomain.local"}
            - Or use web interface at http://localhost:8080
            """);
    }
}
