package com.host.SpringBootAutomationProduction.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.host.SpringBootAutomationProduction.model.postgres.Role;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;

@Component
@PropertySource("classpath:static/settings.ini")
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(User user) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60*24*365).toInstant());

//По поводу принудительной блокировки, попробовать валидировать токен данными пользователя с бд (Или из справочника), если отлитчаются то делать isEnable false

        return JWT.create()
//                .withSubject(user.getUsername())
                .withClaim("roles", user.getRoleNames())  //передать роли
                .withClaim("username", user.getUsername())
                .withClaim("authType", user.getAuthType().name())  //передать тип авторизации
                .withIssuedAt(new Date()) //когда выдан токен
                .withIssuer("Spring-Automation-Production") //кто выдал токен, обычно название приложения
                .withExpiresAt(expirationDate) // срок годности
                .sign(Algorithm.HMAC256(secret)); //секретная строка
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
//                .withSubject("User details")
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

}
