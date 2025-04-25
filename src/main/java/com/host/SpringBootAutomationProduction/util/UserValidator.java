package com.host.SpringBootAutomationProduction.util;

import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import com.host.SpringBootAutomationProduction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
public class UserValidator implements Validator {

    private final UserService userService;

    @Autowired
    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        if (userService.findByUsername(user.getUsername()).isPresent() && !getUserCurDetails().getUsername().equals(user.getUsername()))
            errors.rejectValue("username", "", "Пользователь с таким именем уже существует.");
    }

    private User getUserCurDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCurDetails userCurDetails = (UserCurDetails) authentication.getPrincipal();
        return userCurDetails.getPerson();
    }

}
