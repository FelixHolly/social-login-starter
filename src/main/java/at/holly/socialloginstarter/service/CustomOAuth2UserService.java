package at.holly.socialloginstarter.service;

import at.holly.socialloginstarter.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Custom OAuth2UserService that processes user information from OAuth2 providers.
 *
 * This service demonstrates understanding of:
 * 1. The OAuth2 user info retrieval process
 * 2. How to extract user data from different providers (GitHub, Google, Facebook)
 * 3. Normalizing user data across different provider formats
 * 4. The extension point for custom user processing logic
 *
 * Flow when this is called:
 * 1. User completes authentication at OAuth2 provider
 * 2. Spring Security exchanges authorization code for access token
 * 3. Spring Security calls this service with the access token
 * 4. This service fetches user info from provider's userinfo endpoint
 * 5. We extract and normalize the user data
 * 6. We could save/update user in database here (not implemented for simplicity)
 * 7. Returns OAuth2User which becomes the authenticated principal
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Loads the user from the OAuth2 provider and processes their information.
     *
     * @param userRequest Contains the access token and client registration info
     * @return OAuth2User The authenticated user
     * @throws OAuth2AuthenticationException If user info cannot be retrieved
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Call the default implementation to fetch user info from provider
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Get the registration ID (github, google, facebook, etc.)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        logger.info("Processing OAuth2 login for provider: {}", registrationId);
        logger.debug("User attributes: {}", oAuth2User.getAttributes());

        // Process the user based on the provider
        User user = processOAuth2User(registrationId, oAuth2User);

        // In a real application, you would:
        // 1. Check if user exists in database by providerId
        // 2. If exists, update lastLoginAt and any changed info
        // 3. If new user, save to database
        // 4. Return custom UserPrincipal that implements OAuth2User

        userService.processUserLogin(user);

        logger.info("Successfully processed user: {} from provider: {}", user.getName(), registrationId);

        // Return the OAuth2User - in production, return a custom UserPrincipal
        return oAuth2User;
    }

    /**
     * Extracts user information from OAuth2User based on the provider.
     * Different providers return user data in different formats.
     *
     * @param registrationId The OAuth2 provider ID (github, google, etc.)
     * @param oAuth2User The OAuth2User containing provider-specific attributes
     * @return User object with normalized data
     */
    private User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        return switch (registrationId.toLowerCase()) {
            case "github" -> extractGitHubUser(attributes);
            case "google" -> extractGoogleUser(attributes);
            case "facebook" -> extractFacebookUser(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        };
    }

    /**
     * Extracts user info from GitHub's response format.
     *
     * GitHub returns:
     * - id: numeric user ID
     * - login: username
     * - name: display name
     * - email: email (may be null if not public)
     * - avatar_url: profile picture URL
     */
    private User extractGitHubUser(Map<String, Object> attributes) {
        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String login = (String) attributes.get("login");
        String avatarUrl = (String) attributes.get("avatar_url");

        // GitHub may not provide name or email if user hasn't made them public
        // Use login (username) as fallback for name
        String displayName = (name != null && !name.isEmpty()) ? name : login;

        return new User(providerId, "github", email, displayName, avatarUrl, attributes);
    }

    /**
     * Extracts user info from Google's response format.
     *
     * Google returns:
     * - sub: unique user ID (subject)
     * - email: email address
     * - name: full name
     * - picture: profile picture URL
     * - email_verified: boolean indicating if email is verified
     */
    private User extractGoogleUser(Map<String, Object> attributes) {
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture");

        return new User(providerId, "google", email, name, avatarUrl, attributes);
    }

    /**
     * Extracts user info from Facebook's response format.
     *
     * Facebook returns:
     * - id: unique user ID
     * - email: email address
     * - name: full name
     * - picture.data.url: nested structure for profile picture
     */
    private User extractFacebookUser(Map<String, Object> attributes) {
        String providerId = (String) attributes.get("id");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Facebook returns picture in a nested structure
        String avatarUrl = null;
        if (attributes.containsKey("picture")) {
            Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
            if (picture.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) picture.get("data");
                avatarUrl = (String) data.get("url");
            }
        }

        return new User(providerId, "facebook", email, name, avatarUrl, attributes);
    }
}
