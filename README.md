# Social Login Starter

A professional demonstration of OAuth2 social login implementation using Spring Boot 3 and Spring Security 6, showcasing comprehensive understanding of the OAuth2 Authorization Code Grant flow.

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [OAuth2 Flow Explained](#oauth2-flow-explained)
- [Architecture](#architecture)
- [Setup Instructions](#setup-instructions)
- [Security Best Practices](#security-best-practices)
- [Project Structure](#project-structure)
- [Key Concepts Demonstrated](#key-concepts-demonstrated)
- [Production Considerations](#production-considerations)

## Overview

This project demonstrates a production-ready OAuth2 social login implementation that shows understanding of:

- **OAuth2 Authorization Code Grant flow** - The industry-standard authentication pattern
- **Spring Security OAuth2 Client** - Proper configuration and customization
- **Multi-provider support** - GitHub, Google, Facebook (extensible architecture)
- **Custom user processing** - Extracting and normalizing user data across providers
- **Database persistence with JPA** - H2 database for storing OAuth2 user data
- **Security best practices** - Credential management, CSRF protection, session handling

## Key Features

### 1. OAuth2 Authorization Code Grant Flow
Complete implementation of the OAuth2 flow:
- User authorization at OAuth2 provider
- Authorization code exchange for access token
- User info retrieval from provider
- Session management and authentication

### 2. Custom OAuth2UserService
Demonstrates understanding of the OAuth2 user info endpoint:
- Provider-specific attribute extraction (GitHub, Google, Facebook)
- User data normalization across different providers
- User creation and update logic ("find or create" pattern)
- Extensible architecture for adding new providers

### 3. Database Persistence with H2
Demonstrates understanding of data persistence in OAuth2 applications:
- **JPA Entity mapping** - User entity with proper annotations
- **Spring Data JPA Repository** - Repository pattern for database access
- **Transaction management** - @Transactional for atomic operations
- **Unique constraints** - Composite key on provider + providerId
- **H2 Console** - Web interface to view database (http://localhost:8080/h2-console)

### 4. Security Best Practices
- **No hardcoded credentials** - Environment variables only
- **CSRF protection** - Enabled by default, proper token handling
- **Session fixation protection** - New session on authentication
- **Secure logout** - Session invalidation and cookie clearing
- **Proper .gitignore** - Prevents credential and database commits

### 5. Professional Code Quality
- Comprehensive JavaDoc documentation
- Detailed inline comments explaining OAuth2 concepts
- Clean separation of concerns (Controller, Service, Model)
- Educational code that demonstrates understanding

## OAuth2 Flow Explained

### The Complete Flow (Step by Step)

```
1. User clicks "Login with GitHub" → GET /oauth2/authorization/github

2. Spring Security redirects to GitHub:
   https://github.com/login/oauth/authorize?
     response_type=code
     &client_id=YOUR_CLIENT_ID
     &scope=user:email,read:user
     &state=RANDOM_STATE_FOR_CSRF_PROTECTION
     &redirect_uri=http://localhost:8080/login/oauth2/code/github

3. User authenticates at GitHub and approves permissions

4. GitHub redirects back with authorization code:
   GET /login/oauth2/code/github?code=AUTH_CODE&state=STATE

5. Spring Security exchanges code for access token (backend):
   POST https://github.com/login/oauth/access_token
     grant_type=authorization_code
     &code=AUTH_CODE
     &client_id=YOUR_CLIENT_ID
     &client_secret=YOUR_CLIENT_SECRET
     &redirect_uri=http://localhost:8080/login/oauth2/code/github

6. GitHub responds with access token:
   {
     "access_token": "gho_xxxxxxxxxxxx",
     "token_type": "bearer",
     "scope": "user:email,read:user"
   }

7. Spring Security fetches user info:
   GET https://api.github.com/user
     Authorization: Bearer gho_xxxxxxxxxxxx

8. GitHub returns user data:
   {
     "id": 12345,
     "login": "johndoe",
     "name": "John Doe",
     "email": "john@example.com",
     "avatar_url": "https://avatars.githubusercontent.com/u/12345"
   }

9. CustomOAuth2UserService processes user data
   - Extracts user information
   - Normalizes data (different providers use different formats)
   - Creates/updates user in database (or in-memory store)

10. User is authenticated and redirected to success URL
```

### Why This Flow is Secure

1. **User credentials never touch your application** - User authenticates directly with the OAuth2 provider (GitHub)
2. **Authorization code is single-use** - Can only be exchanged once for an access token
3. **State parameter prevents CSRF** - Random state token validates the response
4. **Client secret stays on backend** - Never exposed to the browser
5. **Access token stays on backend** - Stored in session, not accessible to JavaScript

## Architecture

### Component Overview

```
┌─────────────────┐
│   Controllers   │  - Handle HTTP requests
│                 │  - Extract authentication data
│  HomeController │  - Pass data to views
│ SecureController│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Services     │  - Business logic
│                 │  - User data processing
│  UserService    │  - Provider-specific logic
│  CustomOAuth2   │
│   UserService   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     Models      │  - Data structures
│                 │  - User representation
│      User       │
└─────────────────┘

┌─────────────────┐
│     Config      │  - Security configuration
│                 │  - OAuth2 client setup
│ SecurityConfig  │  - Endpoint protection
└─────────────────┘
```

### Key Classes and Their Responsibilities

| Class | Purpose | Key Concepts |
|-------|---------|--------------|
| `SecurityConfig` | Configure Spring Security | OAuth2 login, endpoint protection, CSRF |
| `CustomOAuth2UserService` | Process OAuth2 user data | Provider-specific attribute extraction |
| `UserService` | User business logic | Find-or-create pattern, user management |
| `User` | User model | OAuth2 user representation |
| `SecureController` | Protected endpoints | @AuthenticationPrincipal usage |
| `HomeController` | Public endpoints | OAuth2 provider links |

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- GitHub account (for OAuth2 app registration)

### 1. Register OAuth2 Application with GitHub

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click "New OAuth App"
3. Fill in the details:
   - **Application name**: Social Login Starter (or your choice)
   - **Homepage URL**: `http://localhost:8080`
   - **Authorization callback URL**: `http://localhost:8080/login/oauth2/code/github`
4. Click "Register application"
5. Copy the **Client ID**
6. Click "Generate a new client secret" and copy it

### 2. Configure Environment Variables

**NEVER commit credentials to version control!**

Set environment variables:

```bash
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret
```

Or create a `.env` file (already in .gitignore):

```bash
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
```

### 3. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run with environment variables inline
GITHUB_CLIENT_ID=xxx GITHUB_CLIENT_SECRET=yyy mvn spring-boot:run
```

### 4. Access the Application

- **Home page**: http://localhost:8080
- **Login with GitHub**: Click the "Login with GitHub" button
- **Secure page**: http://localhost:8080/secure (requires authentication)
- **User profile**: http://localhost:8080/user/profile (requires authentication)
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/sociallogin`
  - Username: `sa`
  - Password: (leave empty)

### 5. Viewing the Database

After logging in, you can view the stored user data:

1. Go to http://localhost:8080/h2-console
2. Use the connection settings above
3. Click "Connect"
4. Run SQL queries to view data:
   ```sql
   -- View all users
   SELECT * FROM USERS;

   -- Count users by provider
   SELECT provider, COUNT(*) as count FROM USERS GROUP BY provider;

   -- View recent logins
   SELECT name, email, provider, last_login_at FROM USERS ORDER BY last_login_at DESC;
   ```

## Security Best Practices

### 1. Credential Management

✅ **DO:**
- Use environment variables for all credentials
- Use secrets management services in production (AWS Secrets Manager, Azure Key Vault)
- Rotate credentials regularly
- Use different credentials for dev/staging/prod

❌ **DON'T:**
- Hardcode credentials in source code
- Commit credentials to version control
- Use the same credentials across environments
- Share credentials in plain text (email, Slack, etc.)

### 2. OAuth2 Scopes

**Principle of Least Privilege**: Request only the scopes you need.

```properties
# GitHub - minimal scopes
spring.security.oauth2.client.registration.github.scope=user:email,read:user

# DON'T request unnecessary permissions
# Bad: scope=repo,admin:org,delete_repo
```

### 3. State Parameter

Spring Security automatically handles state parameter for CSRF protection:
- Generates random state on authorization request
- Validates state on callback
- Rejects mismatched state (prevents CSRF attacks)

### 4. Redirect URI Validation

OAuth2 providers validate redirect URIs:
- Must match exactly what's registered
- Prevents authorization code interception
- Use HTTPS in production

### 5. Session Security

``` java
// Implemented in SecurityConfig
.logout(logout -> logout
    .logoutSuccessUrl("/")
    .invalidateHttpSession(true)    // Clear session
    .deleteCookies("JSESSIONID")    // Remove session cookie
    .permitAll()
);
```

## Project Structure

```
src/main/java/at/holly/socialloginstarter/
├── config/
│   └── SecurityConfig.java              # Spring Security configuration
├── controller/
│   ├── HomeController.java              # Public endpoints
│   └── SecureController.java            # Protected endpoints
├── model/
│   └── User.java                        # User data model
├── service/
│   ├── CustomOAuth2UserService.java     # OAuth2 user processing
│   └── UserService.java                 # User business logic
└── SocialLoginStarterApplication.java   # Main application

src/main/resources/
├── application.properties               # Application configuration
├── templates/                           # Thymeleaf templates
│   ├── index.html                       # Home page
│   ├── secure.html                      # Secure page
│   └── profile.html                     # User profile page
└── static/                              # Static resources

.gitignore                               # Prevents committing secrets
pom.xml                                  # Maven dependencies
README.md                                # This file
```

## Key Concepts Demonstrated

### 1. OAuth2 Authorization Code Grant

The most secure OAuth2 flow for web applications:
- User authorization happens at the provider
- Authorization code exchanged for access token on backend
- Client secret never exposed to browser
- Industry standard for web apps

### 2. Custom OAuth2UserService

Demonstrates understanding of the user info endpoint:

```java
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    // 1. Fetch user from provider (default implementation)
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 2. Extract provider-specific attributes
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    // 3. Normalize user data across providers
    User user = processOAuth2User(registrationId, oAuth2User);

    // 4. Save/update user in database
    userService.processUserLogin(user);

    return oAuth2User;
}
```

### 3. Provider-Specific Attribute Handling

Different providers return user data in different formats:

| Provider | ID Field | Name Field | Email Field | Avatar Field |
|----------|----------|------------|-------------|--------------|
| GitHub   | `id`     | `name`     | `email`     | `avatar_url` |
| Google   | `sub`    | `name`     | `email`     | `picture`    |
| Facebook | `id`     | `name`     | `email`     | `picture.data.url` |

The `CustomOAuth2UserService` handles these differences.

### 4. Spring Security Integration

```java
// In controller - Spring Security injects authenticated user
@GetMapping("/secure")
public String securePage(@AuthenticationPrincipal OAuth2User principal, Model model) {
    // principal contains all OAuth2 user attributes
    String name = principal.getAttribute("name");
    String email = principal.getAttribute("email");
    // ...
}
```

### 5. CSRF Protection

Spring Security enables CSRF protection by default:
- Synchronizer token pattern
- Tokens in forms automatically (Thymeleaf)
- Validates on state-changing requests (POST, PUT, DELETE)

## Production Considerations

### What's Missing for Production (Intentionally)

This is a **demonstration project**. For production, you would add:

#### 1. Database Persistence
```java
// Replace in-memory storage with JPA
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... fields
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
```

#### 2. Custom UserPrincipal
```java
// Return custom principal instead of OAuth2User
public class UserPrincipal implements OAuth2User {
    private User user;
    private Map<String, Object> attributes;
    // Implement OAuth2User methods
}
```

#### 3. Account Linking
- Allow users to link multiple OAuth2 providers
- Match users by email across providers
- Handle primary email changes

#### 4. Token Refresh
- Store refresh tokens securely
- Implement token refresh logic
- Handle token expiration

#### 5. Monitoring and Logging
- Log authentication attempts
- Monitor failed logins
- Track OAuth2 errors
- Alert on anomalies

#### 6. Error Handling
- Custom error pages
- Graceful OAuth2 failure handling
- User-friendly error messages

#### 7. Rate Limiting
- Prevent brute force attacks
- Limit authentication attempts
- Implement backoff strategies

## Learning Resources

- [OAuth 2.0 Simplified](https://aaronparecki.com/oauth-2-simplified/)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [RFC 6749 - OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

## License

This is an educational demonstration project.

---

**Created to demonstrate professional understanding of OAuth2 social login implementation.**
