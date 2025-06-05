package com.host.SpringBootAutomationProduction.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class ReportTemplateParametersDTO {

    private ReportTemplateDTO reportTemplateDTO;
    private Map<String, String> parameters;
}
