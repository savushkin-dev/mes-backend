package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.model.User;
import com.host.SpringBootAutomationProduction.repositories.UsersRepository;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public PersonDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> person = usersRepository.findByUsername(username);

        if (person.isEmpty()){
//            throw new UsernameNotFoundException("Username not found!");
            throw new UserNotFoundException();
        }

        return new UserCurDetails(person.get());
    }
}
