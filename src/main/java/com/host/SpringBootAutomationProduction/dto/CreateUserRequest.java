package com.host.SpringBootAutomationProduction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 30, message = "Имя пользователя должно содержать от 3 до 30 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, max = 50, message = "Пароль должен содержать от 4 до 50 символов")
    private String password;

    private Set<String> roles;
}