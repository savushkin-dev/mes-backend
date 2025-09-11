package com.host.SpringBootAutomationProduction.dto;

import com.host.SpringBootAutomationProduction.model.postgres.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private String name;

    public RoleDTO(Role role) {
        this.name = role.getName();
    }

}