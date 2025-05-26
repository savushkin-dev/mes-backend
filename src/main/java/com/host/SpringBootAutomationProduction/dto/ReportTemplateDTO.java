package com.host.SpringBootAutomationProduction.dto;


import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportTemplateDTO {

    private String reportName;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;
    private String sql;
    private String content;
    private String styles;

}
