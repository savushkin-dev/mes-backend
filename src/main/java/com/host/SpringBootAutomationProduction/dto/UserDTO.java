package com.host.SpringBootAutomationProduction.dto;

import com.host.SpringBootAutomationProduction.model.AuthType;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class UserDTO {

    private int id;
    private String username;
    private AuthType authType;
    private Set<RoleDTO> roles;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.authType = user.getAuthType();
        this.roles = user.getRoles().stream()
                .map(RoleDTO::new)
                .collect(Collectors.toSet());
    }

}
