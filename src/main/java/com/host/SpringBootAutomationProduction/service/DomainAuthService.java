package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.model.AuthResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
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

    @PostConstruct
    public void init() {
        log.info("Domain Auth Service initialized");
        log.info("LDAP URL: {}", ldapUrl);
        log.info("Base DN: {}", baseDn);
        log.info("Default domain: {}", defaultDomain);
    }

    public AuthResponse authenticate(String username, String password, String domain) {
        try {
            if (domain == null || domain.isBlank()) {
                domain = defaultDomain;
            }

            log.debug("Attempting authentication for: {}@{}", username, domain);

            // Format username for AD
            String userPrincipal = username + "@" + domain;
            String userDn = "CN=" + username + ",CN=Users," + baseDn;

            // Try authentication
            boolean isAuthenticated = tryLdapBind(userPrincipal, password) ||
                    tryLdapBind(userDn, password);

            if (isAuthenticated) {
                log.info("Authentication successful for: {}", username);
//                UserInfo userInfo = getUserInfo(username);
//                return AuthResponse.success(
//                        "Authentication successful",
//                        username,
//                        userInfo.displayName(),
//                        userInfo.email(),
//                        userInfo.groups()
//                );
                return AuthResponse.success(
                        "Authentication successful",
                        username,
                        "",
                        "",
                        new ArrayList<>()
                );
            } else {
                log.warn("Authentication failed for: {}", username);
                return AuthResponse.error("Invalid username or password", username);
            }

        } catch (Exception e) {
            log.error("Authentication error for user: {}", username, e);
            return AuthResponse.error("Authentication error: " + e.getMessage(), username);
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

    private UserInfo getUserInfo(String username) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "none");

            DirContext ctx = new InitialDirContext(env);

            String filter = "(&(objectClass=user)(sAMAccountName=" + username + "))";
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{
                    "displayName", "mail", "memberOf", "name"
            });

            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, controls);

            if (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();

                return new UserInfo(
                        getAttributeValue(attrs, "name"),
                        getAttributeValue(attrs, "displayName"),
                        getAttributeValue(attrs, "mail"),
                        getGroups(attrs)
                );
            }

            ctx.close();

        } catch (Exception e) {
            log.warn("Failed to get user info for: {}", username, e);
        }

        return new UserInfo(username, null, null, Collections.emptyList());
    }

    private String getAttributeValue(Attributes attrs, String attributeName) throws NamingException {
        Attribute attr = attrs.get(attributeName);
        return attr != null ? (String) attr.get() : null;
    }

    private List<String> getGroups(Attributes attrs) throws NamingException {
        List<String> groups = new ArrayList<>();
        Attribute memberOf = attrs.get("memberOf");

        if (memberOf != null) {
            NamingEnumeration<?> values = memberOf.getAll();
            while (values.hasMore()) {
                String groupDn = (String) values.next();
                if (groupDn.contains("CN=")) {
                    String cn = groupDn.split(",")[0].replace("CN=", "");
                    groups.add(cn);
                }
            }
        }

        return groups;
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

    private record UserInfo(String username, String displayName, String email, List<String> groups) {}
}
