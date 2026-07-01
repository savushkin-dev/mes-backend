# 🏭 MES Backend

[![Java](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0+-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![JWT](https://img.shields.io/badge/JWT-Authorization-9146FF?logo=json-web-tokens&logoColor=white)](https://jwt.io/)


REST API бэкенд для веб-портала автоматизации производственных процессов. Разработан на Java с использованием Spring Boot, обеспечивает JWT-аутентификацию, интеграцию с корпоративными системами через NTLM, а также предоставляет REST API для управления производственными данными.

### 🛠 Технологический стек

| Компонент | Технология |
|-----------|-----------|
| **Язык** | Java 17 |
| **Фреймворк** | Spring Boot 3.0+ |
| **Безопасность** | Spring Security + JWT |
| **Сборка** | Maven |
| **Интеграции** | NTLM |


### Предварительные требования

- **Java** 17
- **Maven** 3.8 или выше
- **База данных**
- **LDAP сервер** (для корпоративной аутентификации)

### Клонирование репозитория

```bash
git clone https://github.com/savushkin-dev/mes-backend.git
cd mes-backend
```

### ⚙️ Конфигурация окружений

Создайте файл `.env` в корне проекта со следующими переменными:

```env
HTTP_PORT=8080

DB_URL=jdbc:postgresql://localhost:5432/mes_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

LDAP_URL=ldap://localhost:389
LDAP_BASE_DN=DC=example,DC=com
LDAP_DOMAIN=example.com

JWT_SECRET=your_secret_key_here

LOGGING_APP_PATH=/path/to/log
LOGGING_ACCESS_PATH=/path/to/log
```

