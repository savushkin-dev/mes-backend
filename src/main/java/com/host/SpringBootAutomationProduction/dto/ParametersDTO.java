package com.host.SpringBootAutomationProduction.dto;

import com.host.SpringBootAutomationProduction.model.ParametersRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ParametersDTO {
    private Map<String, String> parameters;
}
