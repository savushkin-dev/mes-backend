package com.host.SpringBootAutomationProduction.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ErrorResponse {

    private String message;

    private Date timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = new Date();
    }
}
