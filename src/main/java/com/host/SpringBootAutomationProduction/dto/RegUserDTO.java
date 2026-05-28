package com.host.SpringBootAutomationProduction.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegUserDTO {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 30, message = "Имя пользователя должно содержать от 3 до 30 символов")
    private String username;

    @NotNull(message = "Пароль обязателен")
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, max = 50, message = "Пароль должен содержать от 4 до 50 символов")
    private String password;

    public RegUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
