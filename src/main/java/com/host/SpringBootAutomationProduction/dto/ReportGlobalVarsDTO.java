package com.host.SpringBootAutomationProduction.dto;

import com.host.SpringBootAutomationProduction.util.Encryption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportGlobalVarsDTO {

    private String key;
    private String value;
    private String description;

    public ReportGlobalVarsDTO encrypt() {
        setValue(Encryption.encrypt(getValue()));
        return this;
    }

    public ReportGlobalVarsDTO decrypt() {
        setValue(Encryption.decrypt(getValue()));
        return this;
    }

}
