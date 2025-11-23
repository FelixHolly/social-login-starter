package at.holly.socialloginstarter.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for public endpoints.
 *
 * This demonstrates:
 * 1. Public pages accessible without authentication
 * 2. Conditional rendering based on authentication status
 * 3. Providing login links for different OAuth2 providers
 */
@Controller
public class HomeController {

    /**
     * Home page - accessible to everyone.
     * Shows different content based on whether user is authenticated:
     * - Authenticated: Welcome message with user info
     * - Not authenticated: Login options
     *
     * @param principal The OAuth2 user if authenticated, null otherwise
     * @param model Model for passing data to the view
     * @return The view name
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            // User is authenticated - show their info
            String name = principal.getAttribute("name");
            String login = principal.getAttribute("login");
            model.addAttribute("authenticated", true);
            model.addAttribute("username", name != null ? name : login);
        } else {
            // User is not authenticated
            model.addAttribute("authenticated", false);
        }

        return "index.html";
    }

    /*
     * OAUTH2 LOGIN URLS:
     *
     * Spring Security automatically creates these endpoints for each registered provider:
     *
     * GitHub:   /oauth2/authorization/github
     * Google:   /oauth2/authorization/google
     * Facebook: /oauth2/authorization/facebook
     *
     * The format is always: /oauth2/authorization/{registrationId}
     * where registrationId matches the property key in application.properties
     *
     * The complete OAuth2 flow:
     *
     * 1. User clicks login link -> GET /oauth2/authorization/github
     *
     * 2. Spring Security redirects to GitHub's authorization URL with parameters:
     *    https://github.com/login/oauth/authorize?
     *      response_type=code
     *      &client_id={your_client_id}
     *      &scope=user:email,read:user
     *      &state={random_state_for_csrf_protection}
     *      &redirect_uri=http://localhost:8080/login/oauth2/code/github
     *
     * 3. User authenticates at GitHub and approves the requested scopes
     *
     * 4. GitHub redirects back with authorization code:
     *    GET /login/oauth2/code/github?code={authorization_code}&state={state}
     *
     * 5. Spring Security validates state and exchanges code for access token:
     *    POST https://github.com/login/oauth/access_token
     *      grant_type=authorization_code
     *      &code={authorization_code}
     *      &redirect_uri=http://localhost:8080/login/oauth2/code/github
     *      &client_id={your_client_id}
     *      &client_secret={your_client_secret}
     *
     * 6. GitHub responds with access token:
     *    {
     *      "access_token": "gho_xxxx",
     *      "token_type": "bearer",
     *      "scope": "user:email,read:user"
     *    }
     *
     * 7. Spring Security fetches user info with the access token:
     *    GET https://api.github.com/user
     *      Authorization: Bearer gho_xxxx
     *
     * 8. GitHub responds with user data:
     *    {
     *      "id": 12345,
     *      "login": "johndoe",
     *      "name": "John Doe",
     *      "email": "john@example.com",
     *      "avatar_url": "https://avatars.githubusercontent.com/u/12345"
     *    }
     *
     * 9. CustomOAuth2UserService processes the user data
     *
     * 10. User is authenticated and redirected to the success URL
     */
}
