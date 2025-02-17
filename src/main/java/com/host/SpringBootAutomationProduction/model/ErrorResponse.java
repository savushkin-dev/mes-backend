package com.host.SpringBootAutomationProduction.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ErrorResponse {

    private String message;

    private Date timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = new Date();
    }
}
