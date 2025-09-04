package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.exceptions.NotFoundException;
import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.model.AuthType;
import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.repositories.postgres.RoleRepository;
import com.host.SpringBootAutomationProduction.repositories.postgres.UserRepository;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private User getUserOrgDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCurDetails userCurDetails = (UserCurDetails) authentication.getPrincipal();
        return userCurDetails.getPerson();
    }

    @Transactional
    public User createNtlmUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setAuthType(AuthType.NTLM);
        user.setPassword(null); // Пароль не хранится для NTLM пользователей

        Role role = roleRepository.findByName("ROLE_USER").get();
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }

    @Transactional
    public User createStandardUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setAuthType(AuthType.STANDARD);
        user.setPassword(passwordEncoder.encode(password));

        Role role = roleRepository.findByName("ROLE_USER").get();
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }

    public User assignRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException(roleName));

        user.addRole(role);
        return userRepository.save(user);
    }

    public User assignRolesToUser(String username, Set<String> roleNames) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        List<Role> roles = roleRepository.findByNames(new ArrayList<>(roleNames));
        if (roles.size() != roleNames.size()) {
            throw new NotFoundException("Some roles not found");
        }

        user.setRoles(new HashSet<>(roles));
        return userRepository.save(user);
    }

}
