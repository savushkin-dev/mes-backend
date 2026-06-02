package com.host.SpringBootAutomationProduction.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private int accessTokenExpirationMinutes;

    public String generateAccessToken(User user) {
        Date expirationDate = Date.from(
                ZonedDateTime.now().plusMinutes(accessTokenExpirationMinutes).toInstant()
        );

        return JWT.create()
                .withClaim("roles", user.getRoleNames())
                .withClaim("username", user.getUsername())
                .withClaim("authType", user.getAuthType().name())
                .withIssuedAt(new Date())
                .withIssuer("Spring-Automation-Production")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("Spring-Automation-Production")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("username").asString();
    }

    public String extractAuthType(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("Spring-Automation-Production")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("authType").asString();
    }

    public List<String> extractRoles(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("Spring-Automation-Production")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("roles").asList(String.class);
    }
}