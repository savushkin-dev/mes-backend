package com.host.SpringBootAutomationProduction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@Slf4j
@Service
public class DomainAuthService {

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.domain}")
    private String defaultDomain;

    /**
     * Аутентификация пользователя в AD через LDAP
     * Поддерживает форматы:
     * - DOMAIN\\username (NETBIOS) - автоматически преобразуется в полный домен
     * - просто username (используется defaultDomain)
     *
     * @param username - логин пользователя (может быть в формате domain\\username или просто username)
     * @param password - пароль
     * @return true - если аутентификация успешна
     */
    public Boolean authenticate(String username, String password) {
        try {
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                log.warn("Empty username or password");
                return false;
            }

            // Очищаем username от пробелов
            username = username.trim();

            String userPrincipal = null;

            // Проверяем формат DOMAIN\\username (NETBIOS)
            if (username.contains("\\")) {
                String[] parts = username.split("\\\\");
                if (parts.length == 2) {
                    String netbiosDomain = parts[0].trim();
                    String login = parts[1].trim();

                    // Преобразуем NETBIOS в полный домен
                    String fullDomain = convertNetbiosToFullDomain(netbiosDomain);
                    if (fullDomain != null) {
                        userPrincipal = login + "@" + fullDomain;
                        log.debug("NETBIOS format detected: {} -> {}", username, userPrincipal);
                    } else {
                        // Если не знаем маппинг, используем defaultDomain
                        userPrincipal = login + "@" + defaultDomain;
                        log.debug("NETBIOS format detected (unknown domain): {} -> {}", username, userPrincipal);
                    }
                }
            } else {
                // Просто username, добавляем домен
                userPrincipal = username + "@" + defaultDomain;
                log.debug("Simple username format: {} -> {}", username, userPrincipal);
            }

            if (userPrincipal == null) {
                log.error("Could not determine user principal for: {}", username);
                return false;
            }

            log.debug("Attempting authentication with principal: {}", userPrincipal);

            boolean isAuthenticated = tryLdapBind(userPrincipal, password);

            if (isAuthenticated) {
                log.info("User {} authenticated successfully", userPrincipal);
            } else {
                log.warn("Authentication failed for {}", userPrincipal);
            }

            return isAuthenticated;

        } catch (Exception e) {
            log.error("Authentication error for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Преобразование NETBIOS имени домена в полное DNS имя
     * Универсальный метод:
     * - stolinpf -> stolinpf.bmk.by
     * - pinskpf -> pinskpf.bmk.by
     * - bmk -> bmk.by
     */
    private String convertNetbiosToFullDomain(String netbiosDomain) {
        if (netbiosDomain == null || netbiosDomain.isBlank()) {
            return null;
        }

        String domainLower = netbiosDomain.toLowerCase().trim();

        // Если домен уже содержит точку, возможно это уже полное имя
        if (domainLower.contains(".")) {
            return domainLower;
        }

        // Особый случай: если это корневой домен bmk
        if (domainLower.equals("bmk")) {
            return "bmk.by";
        }

        // Для всех остальных: добавляем .bmk.by
        // stolinpf -> stolinpf.bmk.by
        // pinskpf -> pinskpf.bmk.by
        return domainLower + ".bmk.by";
    }

    /**
     * Попытка привязаться к LDAP с указанными учетными данными
     */
    private boolean tryLdapBind(String principal, String password) {
        DirContext context = null;
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, principal);
            env.put(Context.SECURITY_CREDENTIALS, password);

            context = new InitialDirContext(env);
            log.debug("LDAP bind successful for: {}", principal);
            return true;

        } catch (Exception e) {
            log.debug("LDAP bind failed for {}: {}", principal, e.getMessage());
            return false;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    log.warn("Error closing context", e);
                }
            }
        }
    }
}