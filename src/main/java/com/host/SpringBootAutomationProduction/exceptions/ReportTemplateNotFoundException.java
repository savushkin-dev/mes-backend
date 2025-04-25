package com.host.SpringBootAutomationProduction.exceptions;

public class ReportTemplateNotFoundException extends RuntimeException {
    public ReportTemplateNotFoundException(String message) {
        super(message);
    }
}
