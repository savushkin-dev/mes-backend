package com.host.SpringBootAutomationProduction.configuration;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.host.SpringBootAutomationProduction.exceptions.UserNotFoundException;
import com.host.SpringBootAutomationProduction.security.JWTUtil;
import com.host.SpringBootAutomationProduction.service.PersonDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JWTUtil jwtUtil;

    private final PersonDetailsService personDetailsService;


    @Autowired
    public JWTFilter(AuthenticationEntryPoint authenticationEntryPoint, JWTUtil jwtUtil, PersonDetailsService personDetailsService) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtUtil = jwtUtil;
        this.personDetailsService = personDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && !authHeader.isBlank()) {
            String jwt = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

            if (jwt.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");
                return;
            }

            try {
                String username = jwtUtil.validateTokenAndRetrieveClaim(jwt);
                String authType = jwtUtil.extractAuthType(jwt); // Получаем тип аутентификации

                UserDetails userDetails = personDetailsService.loadUserByUsername(username);

                //проверяем соответствие типа аутентификации
                if (userDetails.getPassword() == null && !"NTLM".equals(authType)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication type");
                    return;
                }

                if (userDetails.getPassword() != null && !"STANDARD".equals(authType)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication type");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                userDetails.getPassword(), // credentials всегда null для JWT
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (JWTVerificationException e) {
                response.setHeader("error","Invalid JWT Token");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (UsernameNotFoundException e) {
                response.setHeader("error","User not found");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            authenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException(e.getMessage()));
        }


    }

}
