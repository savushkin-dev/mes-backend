package com.host.SpringBootAutomationProduction.controller;


import com.host.SpringBootAutomationProduction.dto.LoginRequestDTO;
import com.host.SpringBootAutomationProduction.dto.LoginResponseDTO;
import com.host.SpringBootAutomationProduction.dto.UserDTO;
import com.host.SpringBootAutomationProduction.exceptions.UserNotCreatedException;
import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.security.JWTUtil;
import com.host.SpringBootAutomationProduction.service.DomainAuthService;
import com.host.SpringBootAutomationProduction.service.PersonDetailsService;
import com.host.SpringBootAutomationProduction.service.RegistrationService;
import com.host.SpringBootAutomationProduction.service.UserService;
import com.host.SpringBootAutomationProduction.util.UserValidator;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    private final UserValidator userValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final DomainAuthService domainAuthService;

    @Autowired
    public AuthenticationController(UserValidator userValidator, RegistrationService registrationService, JWTUtil jwtUtil, AuthenticationManager authenticationManager, PersonDetailsService personDetailsService, UserService userService, DomainAuthService domainAuthService) {
        this.userValidator = userValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.domainAuthService = domainAuthService;
    }


    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponseDTO> performAuthentication(@RequestBody LoginRequestDTO loginRequestDto) {


        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();
        String domain = "bmk.by";

        // Сначала пробуем NTLM аутентификацию
        boolean isNtlmAuthenticated = domainAuthService
                .authenticate(username, password, domain);

        User user;

        if (isNtlmAuthenticated) {
            Optional<User> findUser = userService.findByUsername(username);

            // Первый вход - создаем пользователя
            user = findUser.orElseGet(() -> userService.createNtlmUser(username));
        } else {
            // Иначе стандартную аутентификацию
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            Optional<User> findUser = userService.findByUsername(username);
            user = findUser.orElseThrow(() -> new UserNotFoundException(username));

        }

        // Генерация JWT токена
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }


    @PostMapping("/registration")
    public ResponseEntity<LoginResponseDTO> performRegistration(@RequestBody @Valid UserDTO userDTO,
                                                                BindingResult bindingResult) {
        User user = convertToPerson(userDTO);

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

        registrationService.register(user);

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    private User convertToPerson(UserDTO userDTO) {
        return new ModelMapper().map(userDTO, User.class);
    }


}
