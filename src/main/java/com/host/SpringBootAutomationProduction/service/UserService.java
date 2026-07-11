package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.dto.CreateUserRequest;
import com.host.SpringBootAutomationProduction.dto.UserDTO;
import com.host.SpringBootAutomationProduction.exceptions.NotFoundException;
import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.model.AuthType;
import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.repositories.postgres.RoleRepository;
import com.host.SpringBootAutomationProduction.repositories.postgres.UserRepository;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, RoleService roleService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.refreshTokenService = refreshTokenService;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    @Transactional
    public UserDTO updateUserRoles(int userId, Set<String> newRoles) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Set<Role> roles = roleService.findByRoleNames(newRoles);
        user.setRoles(roles);
        userRepository.save(user);
        return new UserDTO(user);
    }

    private User getUserOrgDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserCurDetails userCurDetails = (UserCurDetails) authentication.getPrincipal();
        return userCurDetails.getPerson();
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (request.isNtlm()) {
            return createNtlmUser(request.getUsername(), request.getRoles());
        }
        return createStandardUser(
                request.getUsername(),
                request.getPassword(),
                request.getRoles()
        );
    }

    @Transactional
    public User createNtlmUser(String username, Set<String> roleNames) {
        User user = new User();
        user.setUsername(username);
        user.setAuthType(AuthType.NTLM);
        user.setPassword("-"); // Пароль не хранится для NTLM пользователей
        Set<Role> roles = getRolesWithViewer(roleNames);
        user.setRoles(roles);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User NTLM registered successfully with username: {}", user.getUsername());
        return user;
    }


    @Transactional
    public User createStandardUser(String username, String password, Set<String> roleNames) {
        if (findByUsername(username).isPresent()) {
            throw new RuntimeException("Пользователь с именем '" + username + "' уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setAuthType(AuthType.STANDARD);
        user.setPassword(passwordEncoder.encode(password));

        // Добавляем роли (обязательно ROLE_VIEWER + переданные)
        Set<Role> roles = getRolesWithViewer(roleNames);
        user.setRoles(roles);
        user.setEnabled(true);

        userRepository.save(user);
        log.info("User STANDARD registered successfully with username: {} and roles: {}", username, roleNames);
        return user;
    }

    // Вспомогательный метод для формирования ролей с обязательным ROLE_VIEWER
    private Set<Role> getRolesWithViewer(Set<String> roleNames) {
        Set<String> allRoles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            allRoles.addAll(roleNames);
        }
        allRoles.add("ROLE_VIEWER");

        return roleService.findByRoleNames(allRoles);
    }

    @Transactional
    public void deleteUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Не даем удалить самого себя
        User currentUser = getUserOrgDetails();
        if (currentUser.getId() == userId) {
            throw new RuntimeException("Cannot delete your own account");
        }

        user.getRoles().clear();
        userRepository.save(user);

        userRepository.delete(user);

        log.info("User deleted successfully: {} (id: {}) by admin: {}", user.getUsername(), userId, currentUser.getUsername());
    }

    @Transactional
    public void disableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        user.setEnabled(false);
        userRepository.save(user);

        // Отзываем все refresh токены
        refreshTokenService.revokeAllUserTokens(username);

        log.info("User {} disabled", username);
    }

    @Transactional
    public void enableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        user.setEnabled(true);
        userRepository.save(user);

        log.info("User {} enabled", username);
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
