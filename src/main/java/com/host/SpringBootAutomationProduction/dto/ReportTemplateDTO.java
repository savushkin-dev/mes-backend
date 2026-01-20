package com.host.SpringBootAutomationProduction.dto;


import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.util.Encryption;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
public class ReportTemplateDTO {

    private int id;
    private String reportName;
    private String reportCategory;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;
    private String sql;
    private String content;
    private String styles;
    private String parameters;
    private String script;
    private boolean sqlMode;
    private String dataBands;
    private boolean bookOrientation;

    public ReportTemplateDTO encrypt() {
        setDbPassword(Encryption.encrypt(getDbPassword()));
        return this;
    }

    public ReportTemplateDTO decrypt() {
        setDbPassword(Encryption.decrypt(getDbPassword()));
        return this;
    }

    @Override
    public String toString() {
        return "ReportTemplateDTO{" +
                "sqlMode=" + sqlMode +
                ", parameters='" + parameters + '\'' +
                ", sql='" + sql + '\'' +
                ", dbDriver='" + dbDriver + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbUrl='" + dbUrl + '\'' +
                ", reportCategory='" + reportCategory + '\'' +
                ", reportName='" + reportName + '\'' +
                '}';
    }
}
