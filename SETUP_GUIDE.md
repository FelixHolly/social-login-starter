# Setup Guide

## Prerequisites

- Java 25
- Maven 3.6+
- GitHub account for OAuth2 app registration

## OAuth2 Application Registration

### Creating a GitHub OAuth App

1. Navigate to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click "New OAuth App"
3. Fill in the application details:
   - **Application name**: Social Login Starter
   - **Homepage URL**: `http://localhost:8080`
   - **Authorization callback URL**: `http://localhost:8080/login/oauth2/code/github`
4. Click "Register application"
5. Copy the **Client ID**
6. Click "Generate a new client secret" and copy it

### Configuring Credentials

**CRITICAL: Never commit credentials to version control**

Set environment variables:

```bash
export GITHUB_CLIENT_ID=<client-id-from-github>
export GITHUB_CLIENT_SECRET=<client-secret-from-github>
```

Alternatively, create a `.env` file (already in .gitignore):

```bash
GITHUB_CLIENT_ID=<client-id-from-github>
GITHUB_CLIENT_SECRET=<client-secret-from-github>
```

## Building and Running

### Build the Project

```bash
mvn clean package
```

### Run the Application

```bash
mvn spring-boot:run
```

Or with inline environment variables:

```bash
GITHUB_CLIENT_ID=xxx GITHUB_CLIENT_SECRET=yyy mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

## Accessing the Application

- **Home page**: http://localhost:8080
- **Login with GitHub**: Click the "Login with GitHub" button
- **Secure page**: http://localhost:8080/secure (requires authentication)
- **User profile**: http://localhost:8080/user/profile (requires authentication)
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/sociallogin`
  - Username: `sa`
  - Password: (leave empty)

## Database Console

After logging in, the H2 console can be used to view stored user data:

1. Navigate to http://localhost:8080/h2-console
2. Enter connection settings (see above)
3. Click "Connect"
4. Run SQL queries:

```sql
-- View all users
SELECT * FROM USERS;

-- Count users by provider
SELECT provider, COUNT(*) as count FROM USERS GROUP BY provider;

-- View recent logins
SELECT name, email, provider, last_login_at FROM USERS ORDER BY last_login_at DESC;
```

## Security Considerations

### OAuth2 Scopes

Request only the minimum required scopes:

```properties
# GitHub - minimal scopes
spring.security.oauth2.client.registration.github.scope=user:email,read:user
```

### H2 Console Security

**IMPORTANT**: The H2 console is enabled for development only. In production:
- Set `spring.h2.console.enabled=false`
- Or restrict access to admin users only
- Use a production database (PostgreSQL, MySQL, etc.)

## Troubleshooting

### Application Won't Start

**Error**: No OAuth2 client credentials configured

**Solution**: Ensure environment variables are set:
```bash
echo $GITHUB_CLIENT_ID
echo $GITHUB_CLIENT_SECRET
```

### OAuth2 Login Fails

**Error**: Redirect URI mismatch

**Solution**: Verify the callback URL in GitHub OAuth App settings matches:
```
http://localhost:8080/login/oauth2/code/github
```

### Database Connection Error

**Error**: Cannot open database file

**Solution**: Ensure the `data/` directory exists and has write permissions:
```bash
mkdir -p data
chmod 755 data
```

## Adding Additional OAuth2 Providers

### Google

1. Create OAuth2 credentials at [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
3. Uncomment Google configuration in `application.properties`
4. Set environment variables:
   ```bash
   export GOOGLE_CLIENT_ID=<google-client-id>
   export GOOGLE_CLIENT_SECRET=<google-client-secret>
   ```

### Facebook

1. Create an app at [Facebook for Developers](https://developers.facebook.com/)
2. Add OAuth redirect URI: `http://localhost:8080/login/oauth2/code/facebook`
3. Uncomment Facebook configuration in `application.properties`
4. Set environment variables:
   ```bash
   export FACEBOOK_CLIENT_ID=<facebook-app-id>
   export FACEBOOK_CLIENT_SECRET=<facebook-app-secret>
   ```
