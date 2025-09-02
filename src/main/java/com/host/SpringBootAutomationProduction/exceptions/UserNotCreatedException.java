package com.host.SpringBootAutomationProduction.exceptions;

public class UserNotCreatedException extends RuntimeException {

    public UserNotCreatedException(){
    }

    public UserNotCreatedException(String message){
        super(message);
    }
}
