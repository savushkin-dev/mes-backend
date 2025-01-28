package com.host.SpringBootAutomationProduction.exceptions;

public class UserNotUpdatedException extends RuntimeException {
    public UserNotUpdatedException(String message){
        super(message);
    }
}
