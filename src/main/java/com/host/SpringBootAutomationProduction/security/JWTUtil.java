package com.host.SpringBootAutomationProduction.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
@PropertySource("classpath:static/settings.ini")
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(String username) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60*24*365).toInstant());

        return JWT.create()
                .withSubject("User details")
                .withClaim("username", username)  //пары ключ значение
                .withIssuedAt(new Date()) //когда выдан токен
                .withIssuer("Spring-Automation-Production") //кто выдал токен, обычно название приложения
                .withExpiresAt(expirationDate) // срок годности
                .sign(Algorithm.HMAC256(secret)); //секретная строка
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("Spring-Automation-Production")
                .build();
        DecodedJWT jwt = verifier.verify(token);

        return jwt.getClaim("username").asString();
    }

}
