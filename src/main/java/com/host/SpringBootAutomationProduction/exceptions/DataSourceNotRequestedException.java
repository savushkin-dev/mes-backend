package com.host.SpringBootAutomationProduction.exceptions;

public class DataSourceNotRequestedException extends RuntimeException {
    public DataSourceNotRequestedException(String message) {
        super(message);
    }
}
