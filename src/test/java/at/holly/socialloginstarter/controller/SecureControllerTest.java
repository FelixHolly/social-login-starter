package at.holly.socialloginstarter.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SecureController.
 *
 * Tests demonstrate understanding of:
 * - @WebMvcTest for controller testing
 * - Spring Security test support
 * - Testing with OAuth2 authentication
 * - Model attribute verification
 * - Modern Spring Boot 3.4+ bean mocking with @MockitoBean
 */
@WebMvcTest(SecureController.class)
@DisplayName("SecureController Integration Tests")
class SecureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @DisplayName("Should redirect unauthenticated users to login")
    void shouldRedirectUnauthenticatedUsersToLogin() throws Exception {
        // When - accessing secure page without authentication
        mockMvc.perform(get("/secure"))
            // Then - should redirect to login
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should display secure page for authenticated OAuth2 user")
    void shouldDisplaySecurePageForAuthenticatedUser() throws Exception {
        // Given - authenticated GitHub OAuth2 user
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 12345);
        attributes.put("login", "johndoe");
        attributes.put("name", "John Doe");
        attributes.put("email", "john@example.com");
        attributes.put("avatar_url", "https://avatars.githubusercontent.com/u/12345");

        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
            List.of(new OAuth2UserAuthority(attributes)),
            attributes,
            "id"
        );

        // When - accessing secure page with OAuth2 authentication
        mockMvc.perform(get("/secure")
                .with(oauth2Login().oauth2User(oauth2User)))
            // Then - should return secure page with user data
            .andExpect(status().isOk())
            .andExpect(view().name("secure.html"))
            .andExpect(model().attribute("name", "John Doe"))
            .andExpect(model().attribute("email", "john@example.com"))
            .andExpect(model().attribute("avatarUrl", "https://avatars.githubusercontent.com/u/12345"))
            .andExpect(model().attributeExists("attributes"));
    }

    @Test
    @DisplayName("Should use login as name when name not provided")
    void shouldUseLoginAsNameWhenNameNotProvided() throws Exception {
        // Given - OAuth2 user without name
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 12345);
        attributes.put("login", "johndoe");
        attributes.put("email", "john@example.com");
        // name is null

        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
            List.of(new OAuth2UserAuthority(attributes)),
            attributes,
            "id"
        );

        // When - accessing secure page
        mockMvc.perform(get("/secure")
                .with(oauth2Login().oauth2User(oauth2User)))
            // Then - login should be used as name
            .andExpect(status().isOk())
            .andExpect(model().attribute("name", "johndoe"));
    }

    @Test
    @DisplayName("Should use Google picture for avatar when avatar_url not present")
    void shouldUseGooglePictureForAvatar() throws Exception {
        // Given - Google OAuth2 user
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google-123");
        attributes.put("name", "Jane Smith");
        attributes.put("email", "jane@example.com");
        attributes.put("picture", "https://lh3.googleusercontent.com/photo.jpg");

        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
            List.of(new OAuth2UserAuthority(attributes)),
            attributes,
            "sub"
        );

        // When - accessing secure page
        mockMvc.perform(get("/secure")
                .with(oauth2Login().oauth2User(oauth2User)))
            // Then - Google picture should be used
            .andExpect(status().isOk())
            .andExpect(model().attribute("avatarUrl", "https://lh3.googleusercontent.com/photo.jpg"));
    }

    @Test
    @DisplayName("Should display user profile page")
    @WithMockUser
    void shouldDisplayUserProfilePage() throws Exception {
        // When - accessing profile page with authenticated user
        mockMvc.perform(get("/user/profile"))
            // Then - should return profile page
            .andExpect(status().isOk())
            .andExpect(view().name("profile.html"))
            .andExpect(model().attributeExists("username"))
            .andExpect(model().attributeExists("authorities"))
            .andExpect(model().attribute("provider", "Form Login"));
    }

    @Test
    @DisplayName("Should identify OAuth2 authentication in profile page")
    void shouldIdentifyOAuth2Authentication() throws Exception {
        // Given - OAuth2 authenticated user
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 12345);
        attributes.put("login", "johndoe");
        attributes.put("name", "John Doe");

        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
            List.of(new OAuth2UserAuthority(attributes)),
            attributes,
            "id"
        );

        // When - accessing profile page
        mockMvc.perform(get("/user/profile")
                .with(oauth2Login().oauth2User(oauth2User)))
            // Then - should identify as OAuth2
            .andExpect(status().isOk())
            .andExpect(model().attribute("provider", "OAuth2"))
            .andExpect(model().attributeExists("attributes"));
    }
}
