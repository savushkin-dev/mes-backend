package com.host.SpringBootAutomationProduction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ReportParamMetaRespDTO {

    private String parameters;
    private String layoutParams;
}
