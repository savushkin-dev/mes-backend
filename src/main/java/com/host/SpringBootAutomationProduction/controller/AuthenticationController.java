package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.dto.LoginRequestDTO;
import com.host.SpringBootAutomationProduction.dto.LoginResponseDTO;
import com.host.SpringBootAutomationProduction.dto.RefreshRequestDTO;
import com.host.SpringBootAutomationProduction.dto.RegUserDTO;
import com.host.SpringBootAutomationProduction.dto.UserDTO;
import com.host.SpringBootAutomationProduction.exceptions.UserNotCreatedException;
import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.model.postgres.RefreshToken;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.security.JWTUtil;
import com.host.SpringBootAutomationProduction.service.DomainAuthService;
import com.host.SpringBootAutomationProduction.service.PersonDetailsService;
import com.host.SpringBootAutomationProduction.service.RefreshTokenService;
import com.host.SpringBootAutomationProduction.service.UserService;
import com.host.SpringBootAutomationProduction.util.UserValidator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    private final UserValidator userValidator;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final DomainAuthService domainAuthService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationController(UserValidator userValidator, JWTUtil jwtUtil,
                                    AuthenticationManager authenticationManager,
                                    PersonDetailsService personDetailsService,
                                    UserService userService,
                                    DomainAuthService domainAuthService,
                                    RefreshTokenService refreshTokenService) {
        this.userValidator = userValidator;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.domainAuthService = domainAuthService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponseDTO> performAuthentication(@Valid @RequestBody LoginRequestDTO loginRequestDto) {

        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();
        String domain = "bmk.by";

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new BadCredentialsException("Логин и пароль не могут быть пустыми");
        }

        boolean isNtlmAuthenticated = domainAuthService
                .authenticate(username, password, domain);

        User user;

        if (isNtlmAuthenticated) {
            Optional<User> findUser = userService.findByUsername(username);
            user = findUser.orElseGet(() -> userService.createNtlmUser(username));
        } else {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            Optional<User> findUser = userService.findByUsername(username);
            user = findUser.orElseThrow(() -> new UserNotFoundException(username));
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshRequestDTO request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = userService.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null) {
            String token = authorizationHeader;
            try {
                String username = jwtUtil.validateTokenAndRetrieveClaim(token);
                refreshTokenService.revokeAllUserTokens(username);
            } catch (Exception e) {
                log.warn("Error during logout: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok("Logout successful");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/registration")
    public ResponseEntity<LoginResponseDTO> performRegistration(@RequestBody @Valid RegUserDTO regUserDTO,
                                                                BindingResult bindingResult) {
        User user = convertToPerson(regUserDTO);

        userValidator.validate(user, bindingResult);

        if (bindingResult.hasErrors()){
            StringBuilder errorMessage = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for(FieldError error: errors){
                errorMessage.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append(";");
            }
            throw new UserNotCreatedException(errorMessage.toString());
        }

        user = userService.createStandardUser(user.getUsername(), user.getPassword());

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken.getToken()));
    }

    private User convertToPerson(UserDTO userDTO) {
        return new ModelMapper().map(userDTO, User.class);
    }

    private User convertToPerson(RegUserDTO userDTO) {
        return new ModelMapper().map(userDTO, User.class);
    }
}