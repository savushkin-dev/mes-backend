package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.dto.CreateUserRequest;
import com.host.SpringBootAutomationProduction.exceptions.NotFoundException;
import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.model.AuthType;
import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.repositories.postgres.RoleRepository;
import com.host.SpringBootAutomationProduction.repositories.postgres.UserRepository;
import com.host.SpringBootAutomationProduction.security.UserCurDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Set<String> roleNames;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        Role testRole = new Role();
        testRole.setF_ID(1);
        testRole.setName("ROLE_VIEWER");

        roles = new HashSet<>();
        roles.add(testRole);

        roleNames = new HashSet<>();
        roleNames.add("ROLE_VIEWER");

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setAuthType(AuthType.STANDARD);
        testUser.setRoles(roles);
        testUser.setEnabled(true);
    }

    /**
     * Проверяет, что при создании пользователя с уже существующим username выбрасывается исключение.
     * Ожидается: RuntimeException с сообщением о существующем пользователе
     */
    @Test
    @DisplayName("Should throw exception when creating user with existing username")
    void shouldThrowExceptionWhenCreatingUserWithExistingUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() ->
                userService.createStandardUser("testuser", "password", roleNames)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь с именем 'testuser' уже существует");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Проверяет, что при удалении самого себя выбрасывается исключение.
     * Ожидается: RuntimeException с сообщением о невозможности удаления
     */
    @Test
    @DisplayName("Should throw exception when trying to delete own account")
    void shouldThrowExceptionWhenTryingToDeleteOwnAccount() {
        UserCurDetails userCurDetails = mock(UserCurDetails.class);
        User currentUser = new User();
        currentUser.setId(1);
        when(userCurDetails.getPerson()).thenReturn(currentUser);
        when(authentication.getPrincipal()).thenReturn(userCurDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.deleteUser(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot delete your own account");

        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * Проверяет, что при удалении несуществующего пользователя выбрасывается исключение.
     * Ожидается: NotFoundException
     */
    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent user")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: 999");
    }

    /**
     * Проверяет, что при отключении несуществующего пользователя выбрасывается исключение.
     * Ожидается: UserNotFoundException
     */
    @Test
    @DisplayName("Should throw UserNotFoundException when disabling non-existent user")
    void shouldThrowUserNotFoundExceptionWhenDisablingNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.disableUser("nonexistent"))
                .isInstanceOf(UserNotFoundException.class);

        verify(refreshTokenService, never()).revokeAllUserTokens(anyString());
    }

    /**
     * Проверяет, что при включении несуществующего пользователя выбрасывается исключение.
     * Ожидается: UserNotFoundException
     */
    @Test
    @DisplayName("Should throw UserNotFoundException when enabling non-existent user")
    void shouldThrowUserNotFoundExceptionWhenEnablingNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enableUser("nonexistent"))
                .isInstanceOf(UserNotFoundException.class);
    }

    /**
     * Проверяет, что при обновлении ролей несуществующего пользователя выбрасывается исключение.
     * Ожидается: NotFoundException
     */
    @Test
    @DisplayName("Should throw NotFoundException when updating roles for non-existent user")
    void shouldThrowNotFoundExceptionWhenUpdatingRolesForNonExistentUser() {
        Set<String> newRoleNames = new HashSet<>();
        newRoleNames.add("ROLE_ADMIN");

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRoles(999, newRoleNames))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    /**
     * Проверяет, что при назначении роли несуществующему пользователю выбрасывается исключение.
     * Ожидается: UserNotFoundException
     */
    @Test
    @DisplayName("Should throw UserNotFoundException when assigning role to non-existent user")
    void shouldThrowUserNotFoundExceptionWhenAssigningRoleToNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoleToUser("nonexistent", "ROLE_ADMIN"))
                .isInstanceOf(UserNotFoundException.class);
    }

    /**
     * Проверяет, что при назначении несуществующей роли выбрасывается исключение.
     * Ожидается: NotFoundException
     */
    @Test
    @DisplayName("Should throw NotFoundException when assigning non-existent role")
    void shouldThrowNotFoundExceptionWhenAssigningNonExistentRole() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoleToUser("testuser", "ROLE_NONEXISTENT"))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * Проверяет, что пароль со спецсимволами корректно обрабатывается.
     * Ожидается: пароль успешно закодирован
     */
    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        String specialPassword = "P@ssw0rd!@#$%^&*()";
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(specialPassword)).thenReturn("encodedSpecialPassword");
        when(roleService.findByRoleNames(anySet())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createStandardUser("newuser", specialPassword, roleNames);

        assertThat(result).isNotNull();
        verify(passwordEncoder, times(1)).encode(specialPassword);
    }

    /**
     * Проверяет, что при создании NTLM пользователя пароль не кодируется.
     * Ожидается: PasswordEncoder не вызывается
     */
    @Test
    @DisplayName("Should not encode password for NTLM user")
    void shouldNotEncodePasswordForNtlmUser() {
        when(roleService.findByRoleNames(anySet())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.createNtlmUser("ntlmuser", roleNames);

        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * Проверяет, что при создании пользователя всегда добавляется ROLE_VIEWER.
     * Ожидается: в запросе к roleService присутствует ROLE_VIEWER
     */
    @Test
    @DisplayName("Should always include ROLE_VIEWER when creating user")
    void shouldAlwaysIncludeViewerRoleWhenCreatingUser() {
        Set<String> rolesWithoutViewer = new HashSet<>();
        rolesWithoutViewer.add("ROLE_ADMIN");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(roleService.findByRoleNames(anySet())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.createStandardUser("newuser", "password", rolesWithoutViewer);

        verify(roleService, times(1)).findByRoleNames(argThat(roleSet ->
                roleSet.contains("ROLE_VIEWER") && roleSet.contains("ROLE_ADMIN")
        ));
    }

    /**
     * Проверяет, что при отключении пользователя отзываются все refresh токены.
     * Ожидается: refreshTokenService.revokeAllUserTokens вызван
     */
    @Test
    @DisplayName("Should revoke all refresh tokens when disabling user")
    void shouldRevokeAllRefreshTokensWhenDisablingUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(refreshTokenService).revokeAllUserTokens("testuser");

        userService.disableUser("testuser");

        assertThat(testUser.isEnabled()).isFalse();
        verify(refreshTokenService, times(1)).revokeAllUserTokens("testuser");
    }
}