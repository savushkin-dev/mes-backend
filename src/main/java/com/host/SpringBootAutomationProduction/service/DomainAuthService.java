package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.AuthResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

@Slf4j
@Service
public class DomainAuthService {

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.base-dn}")
    private String baseDn;

    @Value("${ldap.domain}")
    private String defaultDomain;

//    @PostConstruct
//    public void init() {
//        log.info("Domain Auth Service initialized");
//        log.info("LDAP URL: {}", ldapUrl);
//        log.info("Base DN: {}", baseDn);
//        log.info("Default domain: {}", defaultDomain);
//    }

    public Boolean authenticate(String username, String password, String domain) {
        try {
            if (domain == null || domain.isBlank()) {
                domain = defaultDomain;
            }

//            log.debug("Attempting authentication for: {}@{}", username, domain);

            String userPrincipal = username + "@" + domain;
            String userDn = "CN=" + username + ",CN=Users," + baseDn;


            boolean isAuthenticated = tryLdapBind(userPrincipal, password) ||
                    tryLdapBind(userDn, password);

            return isAuthenticated;

        } catch (Exception e) {
            return false;
        }
    }

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
            return true;

        } catch (Exception e) {
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


    public String testConnection() {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "none");

            DirContext context = new InitialDirContext(env);
            context.close();

            return "LDAP connection successful to: " + ldapUrl;

        } catch (Exception e) {
            return "LDAP connection failed: " + e.getMessage();
        }
    }

}
