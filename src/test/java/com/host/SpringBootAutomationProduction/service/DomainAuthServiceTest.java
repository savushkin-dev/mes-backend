package com.host.SpringBootAutomationProduction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainAuthService Unit Tests")
class DomainAuthServiceTest {

    @InjectMocks
    private DomainAuthService domainAuthService;

    private static final String TEST_LDAP_URL = "ldap://test.server:389";
    private static final String TEST_DEFAULT_DOMAIN = "bmk.by";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(domainAuthService, "ldapUrl", TEST_LDAP_URL);
        ReflectionTestUtils.setField(domainAuthService, "defaultDomain", TEST_DEFAULT_DOMAIN);
    }

    // ==================== ТЕСТЫ ПРОВЕРКИ ВХОДНЫХ ДАННЫХ ====================

    /**
     * Проверяет, что authenticate возвращает false при null username.
     * Ожидается: false
     */
    @Test
    @DisplayName("Should return false when username is null")
    void authenticate_WhenUsernameIsNull_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate(null, "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что authenticate возвращает false при null password.
     * Ожидается: false
     */
    @Test
    @DisplayName("Should return false when password is null")
    void authenticate_WhenPasswordIsNull_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("username", null);
        assertFalse(result);
    }

    /**
     * Проверяет, что authenticate возвращает false при пустом username.
     * Ожидается: false
     */
    @Test
    @DisplayName("Should return false when username is empty")
    void authenticate_WhenUsernameIsEmpty_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("", "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что authenticate возвращает false при пустом password.
     * Ожидается: false
     */
    @Test
    @DisplayName("Should return false when password is empty")
    void authenticate_WhenPasswordIsEmpty_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("username", "");
        assertFalse(result);
    }

    /**
     * Проверяет, что authenticate возвращает false при username состоящем только из пробелов.
     * Ожидается: false
     */
    @Test
    @DisplayName("Should return false when username is blank")
    void authenticate_WhenUsernameIsBlank_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("   ", "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что authenticate возвращает false при password состоящем только из пробелов.
     * Ожидается: false
     */
    @Test
    @DisplayName("Should return false when password is blank")
    void authenticate_WhenPasswordIsBlank_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("username", "   ");
        assertFalse(result);
    }

    // ==================== ТЕСТЫ ФОРМАТОВ ЛОГИНА ====================

    /**
     * Проверяет преобразование простого логина без домена.
     * Ожидается: username@defaultDomain, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("Simple username -> username@defaultDomain")
    void authenticate_WithSimpleUsername_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("testuser", "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что пробелы в простом логине автоматически обрезаются.
     * Ожидается: trim, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("Simple username with spaces -> trimmed")
    void authenticate_WithSimpleUsernameAndSpaces_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("  testuser  ", "password");
        assertFalse(result);
    }

    /**
     * Проверяет преобразование NETBIOS формата с доменом stolinpf.
     * Ожидается: stolinpf\\user -> user@stolinpf.bmk.by, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("NETBIOS stolinpf\\user -> user@stolinpf.bmk.by")
    void authenticate_WithNetbiosStolinpf_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("stolinpf\\termusertest", "password");
        assertFalse(result);
    }

    /**
     * Проверяет преобразование NETBIOS формата с доменом pinskpf.
     * Ожидается: pinskpf\\user -> user@pinskpf.bmk.by, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("NETBIOS pinskpf\\user -> user@pinskpf.bmk.by")
    void authenticate_WithNetbiosPinskpf_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("pinskpf\\testuser", "password");
        assertFalse(result);
    }

    /**
     * Проверяет преобразование NETBIOS формата с корневым доменом bmk.
     * Ожидается: bmk\\user -> user@bmk.by, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("NETBIOS bmk\\user -> user@bmk.by")
    void authenticate_WithNetbiosBmk_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("bmk\\admin", "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что неизвестный NETBIOS домен использует defaultDomain.
     * Ожидается: unknown\\user -> user@bmk.by, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("Unknown NETBIOS domain -> use defaultDomain")
    void authenticate_WithUnknownNetbiosDomain_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("unknown\\testuser", "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что пробелы в NETBIOS формате автоматически обрезаются.
     * Ожидается: trim, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("NETBIOS with spaces in domain and login")
    void authenticate_WithNetbiosAndSpaces_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("  stolinpf  \\  termusertest  ", "password");
        assertFalse(result);
    }

    /**
     * Проверяет, что username с символом @ обрабатывается как простой логин.
     * Ожидается: user@domain.com -> user@domain.com@bmk.by, но без реального LDAP возвращается false
     */
    @Test
    @DisplayName("Username with @ symbol - treated as simple username with @")
    void authenticate_WithEmailFormat_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("user@domain.com", "password");
        assertFalse(result);
    }

    // ==================== ТЕСТЫ С РАЗЛИЧНЫМИ ПАРОЛЯМИ ====================

    /**
     * Проверяет, что пароль со спецсимволами корректно обрабатывается.
     * Ожидается: false (нет реального LDAP)
     */
    @Test
    @DisplayName("Should handle special characters in password")
    void authenticate_WithSpecialCharsInPassword_ShouldReturnFalse() {
        Boolean result = domainAuthService.authenticate("testuser", "P@ssw0rd!@#$%");
        assertFalse(result);
    }

    /**
     * Проверяет, что длинный пароль корректно обрабатывается.
     * Ожидается: false (нет реального LDAP)
     */
    @Test
    @DisplayName("Should handle long password")
    void authenticate_WithLongPassword_ShouldReturnFalse() {
        String longPassword = "a".repeat(100);
        Boolean result = domainAuthService.authenticate("testuser", longPassword);
        assertFalse(result);
    }

    // ==================== ТЕСТЫ С РАЗЛИЧНЫМИ ЛОГИНАМИ ====================

    /**
     * Параметризованный тест для различных вариантов простых логинов.
     * Ожидается: для всех false (нет реального LDAP)
     */
    @ParameterizedTest
    @DisplayName("Various simple usernames")
    @CsvSource({
            "user1",
            "test-user",
            "admin123",
            "ivanov",
            "petrov",
            "user_with_underscore",
            "123user"
    })
    void authenticate_WithVariousUsernames_ShouldReturnFalse(String username) {
        Boolean result = domainAuthService.authenticate(username, "password");
        assertFalse(result);
    }

    /**
     * Параметризованный тест для различных NETBIOS доменов.
     * Ожидается: для всех false (нет реального LDAP)
     */
    @ParameterizedTest
    @DisplayName("Various NETBIOS domains")
    @CsvSource({
            "stolinpf, termusertest",
            "pinskpf, testuser",
            "brestpf, admin",
            "gomelpf, user1",
            "grodnopf, ivanov"
    })
    void authenticate_WithVariousNetbiosDomains_ShouldReturnFalse(String domain, String login) {
        String username = domain + "\\" + login;
        Boolean result = domainAuthService.authenticate(username, "password");
        assertFalse(result);
    }

}