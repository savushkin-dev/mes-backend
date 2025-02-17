package com.host.SpringBootAutomationProduction.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanDTO {

    @NotEmpty(message = "Не должно быть пустым")
    private String planId;

    @NotEmpty(message = "Не должно быть пустым")
    private String data;

}
