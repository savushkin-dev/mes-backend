package com.host.SpringBootAutomationProduction.dto;


import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.util.Encryption;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
public class ReportTemplateDTO {

    private int id;

    @NotBlank(message = "Название отчета не может быть пустым")
    private String reportName;

    @NotBlank(message = "Категория отчета не может быть пустой")
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
    private String layoutSettingsParams;
    private String layoutParams;

    public ReportTemplateDTO encrypt() {
        setDbPassword(Encryption.encrypt(getDbPassword()));
        setScript(Encryption.encrypt(getScript()));
        setDbUrl(Encryption.encrypt(getDbUrl()));
        setSql(Encryption.encrypt(getSql()));
        setDbUsername(Encryption.encrypt(getDbUsername()));
        return this;
    }

    public ReportTemplateDTO decrypt() {
        setDbPassword(Encryption.decrypt(getDbPassword()));
        setScript(Encryption.decrypt(getScript()));
        setDbUrl(Encryption.decrypt(getDbUrl()));
        setSql(Encryption.decrypt(getSql()));
        setDbUsername(Encryption.decrypt(getDbUsername()));
        return this;
    }

    @Override
    public String toString() {
        return "ReportTemplateDTO{" +
                "id=" + id +
                ", reportName='" + reportName + '\'' +
                ", reportCategory='" + reportCategory + '\'' +
                ", dbUrl='" + dbUrl + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", dbDriver='" + dbDriver + '\'' +
                ", sql='" + sql + '\'' +
                ", content='" + content + '\'' +
                ", styles='" + styles + '\'' +
                ", parameters='" + parameters + '\'' +
                ", script='" + script + '\'' +
                ", sqlMode=" + sqlMode +
                ", dataBands='" + dataBands + '\'' +
                ", bookOrientation=" + bookOrientation +
                ", layoutSettingsParams='" + layoutSettingsParams + '\'' +
                ", layoutParams='" + layoutParams + '\'' +
                '}';
    }
}
