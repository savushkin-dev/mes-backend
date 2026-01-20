package com.host.SpringBootAutomationProduction.dto;


import com.host.SpringBootAutomationProduction.model.AuthType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegUserDTO {

    private String username;
    private String password;

    public RegUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
