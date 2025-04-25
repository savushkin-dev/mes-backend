package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.dto.UserProfileDTO;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import com.host.SpringBootAutomationProduction.util.UserValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class UserController {

    private final UserValidator userValidator;

    @Autowired
    public UserController(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    @GetMapping("/user/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCurDetails userCurDetails = (UserCurDetails) authentication.getPrincipal();
        return ResponseEntity.ok(convertToUserProfileDTO(userCurDetails.getPerson()));
    }

    private UserProfileDTO convertToUserProfileDTO(User user) {
        return new ModelMapper().map(user, UserProfileDTO.class);
    }



}
