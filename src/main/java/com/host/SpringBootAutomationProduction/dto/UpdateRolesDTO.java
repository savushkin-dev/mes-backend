package com.host.SpringBootAutomationProduction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolesDTO {

    private Set<String> roles;

}
