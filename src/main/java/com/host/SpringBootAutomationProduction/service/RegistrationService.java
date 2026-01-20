package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.AuthType;
import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.repositories.postgres.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Transactional
    public void register(User user){
        String encodedPassword = passwordEncoder.encode(user.getPassword()); //шифруем
        System.out.println(encodedPassword);
        user.setPassword(encodedPassword);
        user.setAuthType(AuthType.STANDARD);
        Set<Role> roles = new HashSet<>();
        Role role = roleService.findByName("ROLE_VIEWER").orElseThrow(() -> new RuntimeException("Role ROLE_VIEWER not found"));
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
        log.info("User registered successfully with username: {}", user.getUsername());
    }
}
