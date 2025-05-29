package com.host.SpringBootAutomationProduction.dto;


import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.util.Encryption;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportTemplateDTO {

    private String reportName;
    private String reportCategory;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;
    private String sql;
    private String content;
    private String styles;

    public ReportTemplateDTO encrypt() {
        setDbPassword(Encryption.encrypt(getDbPassword()));
        return this;
    }

    public ReportTemplateDTO decrypt() {
        setDbPassword(Encryption.decrypt(getDbPassword()));
        return this;
    }

}
