package com.host.SpringBootAutomationProduction.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {


    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final JWTFilter jwtFilter;


    @Autowired
    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint, JWTFilter jwtFilter) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.jwtFilter = jwtFilter;
    }


    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/authentication/authenticate").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/authentication/registration").permitAll()
                                .requestMatchers(HttpMethod.GET, "api/scheduler/{planId}").permitAll()
                                .requestMatchers(HttpMethod.GET, "api/scheduler/plansId").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/report/create").permitAll()
                                .requestMatchers(HttpMethod.GET, "api/report/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/report/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "test/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/pdf/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/auth/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "api/auth/**").permitAll()
                                .anyRequest().authenticated()
                )

                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling((exception)-> exception.authenticationEntryPoint(customAuthenticationEntryPoint))

                .httpBasic(withDefaults());

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
