package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.User;
import com.host.SpringBootAutomationProduction.repositories.UsersRepository;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

    private User getUserOrgDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCurDetails userCurDetails = (UserCurDetails) authentication.getPrincipal();
        return userCurDetails.getPerson();
    }

}
