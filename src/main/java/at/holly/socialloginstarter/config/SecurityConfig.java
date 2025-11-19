package at.holly.socialloginstarter.config;

import at.holly.socialloginstarter.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for OAuth2 social login.
 *
 * This configuration demonstrates understanding of:
 * 1. OAuth2 Authorization Code Grant flow
 * 2. Proper endpoint protection with Spring Security
 * 3. CSRF protection (enabled by default)
 * 4. Session management for authenticated users
 * 5. Custom user processing with OAuth2UserService
 */
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    /**
     * Configures the security filter chain for OAuth2 social login.
     *
     * OAuth2 Flow:
     * 1. User accesses protected resource -> redirected to /oauth2/authorization/{registrationId}
     * 2. Spring Security redirects to OAuth2 provider's authorization endpoint
     * 3. User authenticates and grants permissions at the provider
     * 4. Provider redirects back with authorization code to /login/oauth2/code/{registrationId}
     * 5. Spring Security exchanges code for access token at provider's token endpoint
     * 6. Access token is used to fetch user info from provider's userinfo endpoint
     * 7. Custom OAuth2UserService processes user info and creates authenticated principal
     * 8. User is redirected to originally requested resource
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Public endpoints - no authentication required
                                .requestMatchers("/", "/error", "/login**").permitAll()
                                // H2 Console - accessible for development/debugging
                                // IMPORTANT: Disable in production or restrict to admin users
                                .requestMatchers("/h2-console/**").permitAll()
                                // Protected endpoints - require authentication
                                .requestMatchers("/secure/**", "/user/**").authenticated()
                                // All other requests require authentication
                                .anyRequest().authenticated()
                )
                // H2 Console requires frames and CSRF to be disabled
                // Only for development - REMOVE in production
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                // Enable OAuth2 login with custom user service
                // This sets up the authorization endpoints and handles the OAuth2 flow
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/github")  // Default login via GitHub
                        .defaultSuccessUrl("/secure", true)  // Redirect after successful login
                        .userInfoEndpoint(userInfo -> userInfo
                                // Configure our custom OAuth2UserService to process user data
                                // This is called after the access token is obtained
                                .userService(customOAuth2UserService)
                        )
                )
                // Optional: also allow form-based login for testing
                .formLogin(withDefaults())
                // Configure logout to clear session and redirect to home
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        // CSRF protection is enabled by default - important for preventing cross-site attacks
        // (except for H2 console in development)
        // Session fixation protection is enabled by default - prevents session hijacking

        return http.build();
    }

    /*
     * NOTE: Client credentials should NEVER be hardcoded in source code.
     * Always use environment variables or external configuration.
     *
     * Credentials are configured in application.properties using environment variables:
     * spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
     * spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
     *
     * For production:
     * - Use a secrets management service (AWS Secrets Manager, Azure Key Vault, etc.)
     * - Never commit credentials to version control
     * - Rotate credentials regularly
     * - Use different credentials for each environment (dev, staging, prod)
     */
}
