package at.holly.socialloginstarter.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Controller for secure/protected endpoints that require authentication.
 *
 * This demonstrates:
 * 1. Accessing authenticated user information
 * 2. Different ways to retrieve authentication data
 * 3. Extracting OAuth2-specific user attributes
 */
@Controller
public class SecureController {

    /**
     * Secure page that displays authenticated user information.
     *
     * This demonstrates multiple ways to access user info:
     * - @AuthenticationPrincipal annotation to inject OAuth2User directly
     * - Model to pass data to the view
     * - Extracting provider-specific attributes
     *
     * @param principal The authenticated OAuth2 user (injected by Spring Security)
     * @param model Model for passing data to the view
     * @return The view name
     */
    @GetMapping("/secure")
    public String securePage(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            // Get all attributes from the OAuth2 provider
            Map<String, Object> attributes = principal.getAttributes();

            // Extract common user information
            // Note: Different providers use different attribute names
            String name = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            String login = principal.getAttribute("login");  // GitHub username
            String avatarUrl = principal.getAttribute("avatar_url");  // GitHub
            if (avatarUrl == null) {
                avatarUrl = principal.getAttribute("picture");  // Google
            }

            // Pass data to the view
            model.addAttribute("name", name != null ? name : login);
            model.addAttribute("email", email);
            model.addAttribute("avatarUrl", avatarUrl);
            model.addAttribute("attributes", attributes);
        }

        return "secure.html";
    }

    /**
     * Alternative endpoint demonstrating different authentication access patterns.
     *
     * @param authentication The Authentication object (generic)
     * @param model Model for passing data to the view
     * @return The view name
     */
    @GetMapping("/user/profile")
    public String userProfile(Authentication authentication, Model model) {
        if (authentication != null) {
            // Generic approach - works with any authentication type
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authorities", authentication.getAuthorities());

            // Check if it's OAuth2 authentication
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                model.addAttribute("provider", "OAuth2");
                model.addAttribute("attributes", oAuth2User.getAttributes());
            } else {
                model.addAttribute("provider", "Form Login");
            }
        }

        return "profile.html";
    }

    /*
     * KEY CONCEPTS DEMONSTRATED:
     *
     * 1. AUTHENTICATION PRINCIPAL
     *    - @AuthenticationPrincipal injects the authenticated user
     *    - Type-safe access to OAuth2User attributes
     *    - Automatically available in all authenticated requests
     *
     * 2. OAUTH2 USER ATTRIBUTES
     *    - Different providers return different attribute structures
     *    - GitHub: login, name, email, avatar_url
     *    - Google: sub, name, email, picture
     *    - Facebook: id, name, email, picture (nested)
     *
     * 3. SECURITY CONTEXT
     *    - Spring Security stores authentication in SecurityContextHolder
     *    - Available throughout the request lifecycle
     *    - Cleared automatically after response
     *
     * 4. PRODUCTION CONSIDERATIONS
     *    - Don't expose raw attributes to the view (security risk)
     *    - Create a DTO with only necessary fields
     *    - Sanitize all user input before display (XSS protection)
     *    - Use a templating engine with auto-escaping (Thymeleaf)
     */
}
