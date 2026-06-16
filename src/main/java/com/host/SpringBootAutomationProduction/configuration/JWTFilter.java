package com.host.SpringBootAutomationProduction.configuration;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.host.SpringBootAutomationProduction.security.JWTUtil;
import com.host.SpringBootAutomationProduction.service.PersonDetailsService;
import com.host.SpringBootAutomationProduction.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final PersonDetailsService personDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil,
                     PersonDetailsService personDetailsService,
                     RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.personDetailsService = personDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && !authHeader.isBlank()) {
            String jwt = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

            if (jwt.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");
                return;
            }

            try {
                String username = jwtUtil.validateTokenAndRetrieveClaim(jwt);
                String authType = jwtUtil.extractAuthType(jwt);

                // Проверяем наличие валидного refresh token (пользователь не разлогинен)
                if (!refreshTokenService.hasValidRefreshToken(username)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired, please login again");
                    return;
                }

                UserDetails userDetails = personDetailsService.loadUserByUsername(username);

                // Проверка типа аутентификации
                String userAuthType = userDetails.getPassword() == null ? "NTLM" : "STANDARD";
                if (!userAuthType.equals(authType)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication type");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                userDetails.getPassword(),
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (JWTVerificationException e) {
                String message = e.getMessage();
                if (message != null && message.contains("expired")) {
                    log.debug("JWT expired: {}", message);
                } else {
                    log.error("JWT verification error: {}", message);
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT Token");
                return;
            } catch (UsernameNotFoundException e) {
                log.error("User not found: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}