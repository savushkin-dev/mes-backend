package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.User;
import com.host.SpringBootAutomationProduction.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RegistrationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(User user){

        String encodedPassword = passwordEncoder.encode(user.getPassword()); //шифруем
        user.setPassword(encodedPassword);
        usersRepository.save(user);
    }
}
