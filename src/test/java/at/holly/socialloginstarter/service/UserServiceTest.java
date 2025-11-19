package at.holly.socialloginstarter.service;

import at.holly.socialloginstarter.model.User;
import at.holly.socialloginstarter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 *
 * Tests demonstrate understanding of:
 * - JUnit 5 testing framework
 * - Mockito for mocking dependencies
 * - Test isolation (unit tests don't touch database)
 * - AssertJ for fluent assertions
 * - Testing the "find or create" pattern
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User githubUser;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put("id", 12345);
        attributes.put("login", "johndoe");
        attributes.put("avatar_url", "https://example.com/avatar.jpg");

        githubUser = new User(
            "12345",
            "github",
            "john@example.com",
            "John Doe",
            "https://example.com/avatar.jpg",
            attributes
        );
    }

    @Test
    @DisplayName("Should create new user when user does not exist")
    void shouldCreateNewUserWhenUserDoesNotExist() {
        // Given - user does not exist in database
        when(userRepository.findByProviderAndProviderId("github", "12345"))
            .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);  // Simulate database ID generation
                return user;
            });

        // When - processing user login
        User result = userService.processUserLogin(githubUser);

        // Then - user should be saved to database
        verify(userRepository).findByProviderAndProviderId("github", "12345");
        verify(userRepository).save(any(User.class));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getProvider()).isEqualTo("github");
        assertThat(result.getProviderId()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Should update existing user when user already exists")
    void shouldUpdateExistingUserWhenUserAlreadyExists() {
        // Given - user already exists in database
        User existingUser = new User(
            "12345",
            "github",
            "old@example.com",
            "Old Name",
            "https://example.com/old-avatar.jpg",
            new HashMap<>()
        );
        existingUser.setId(1L);
        existingUser.setCreatedAt(LocalDateTime.now().minusDays(30));
        existingUser.setLastLoginAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findByProviderAndProviderId("github", "12345"))
            .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When - processing user login with updated info
        User result = userService.processUserLogin(githubUser);

        // Then - existing user should be updated
        verify(userRepository).findByProviderAndProviderId("github", "12345");
        verify(userRepository).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getId()).isEqualTo(1L);  // ID unchanged
        assertThat(savedUser.getName()).isEqualTo("John Doe");  // Name updated
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");  // Email updated
        assertThat(savedUser.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");  // Avatar updated
        assertThat(savedUser.getLastLoginAt()).isAfterOrEqualTo(existingUser.getLastLoginAt());  // Last login updated
        assertThat(savedUser.getCreatedAt()).isEqualTo(existingUser.getCreatedAt());  // Created date unchanged
    }

    @Test
    @DisplayName("Should find user by provider and provider ID")
    void shouldFindUserByProviderAndProviderId() {
        // Given - user exists in database
        when(userRepository.findByProviderAndProviderId("github", "12345"))
            .thenReturn(Optional.of(githubUser));

        // When - searching for user
        Optional<User> result = userService.getUserByProviderId("github", "12345");

        // Then - user should be found
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        verify(userRepository).findByProviderAndProviderId("github", "12345");
    }

    @Test
    @DisplayName("Should return empty when user not found by provider and provider ID")
    void shouldReturnEmptyWhenUserNotFoundByProviderAndProviderId() {
        // Given - user does not exist
        when(userRepository.findByProviderAndProviderId("github", "99999"))
            .thenReturn(Optional.empty());

        // When - searching for non-existent user
        Optional<User> result = userService.getUserByProviderId("github", "99999");

        // Then - should return empty
        assertThat(result).isEmpty();
        verify(userRepository).findByProviderAndProviderId("github", "99999");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given - user exists with email
        when(userRepository.findByEmail("john@example.com"))
            .thenReturn(Optional.of(githubUser));

        // When - searching by email
        Optional<User> result = userService.getUserByEmail("john@example.com");

        // Then - user should be found
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Given - multiple users in database
        User googleUser = new User("54321", "google", "jane@example.com",
            "Jane Smith", "https://example.com/jane.jpg", new HashMap<>());
        when(userRepository.findAll())
            .thenReturn(List.of(githubUser, googleUser));

        // When - getting all users
        List<User> result = userService.getAllUsers();

        // Then - all users should be returned
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getName)
            .containsExactly("John Doe", "Jane Smith");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should get user count")
    void shouldGetUserCount() {
        // Given - users in database
        when(userRepository.count()).thenReturn(42L);

        // When - getting user count
        long count = userService.getUserCount();

        // Then - correct count should be returned
        assertThat(count).isEqualTo(42L);
        verify(userRepository).count();
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        // Given - user to delete
        githubUser.setId(1L);

        // When - deleting user
        userService.deleteUser(githubUser);

        // Then - user should be deleted from repository
        verify(userRepository).delete(githubUser);
    }

    @Test
    @DisplayName("Should handle multiple logins from same user")
    void shouldHandleMultipleLoginsFromSameUser() {
        // Given - user logs in first time
        when(userRepository.findByProviderAndProviderId("github", "12345"))
            .thenReturn(Optional.empty())  // First login - not found
            .thenReturn(Optional.of(githubUser));  // Second login - found
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                if (user.getId() == null) {
                    user.setId(1L);
                }
                return user;
            });

        // When - user logs in twice
        User firstLogin = userService.processUserLogin(githubUser);
        User secondLogin = userService.processUserLogin(githubUser);

        // Then - user created on first login, updated on second
        verify(userRepository, times(2)).findByProviderAndProviderId("github", "12345");
        verify(userRepository, times(2)).save(any(User.class));

        assertThat(firstLogin.getId()).isEqualTo(1L);
        assertThat(secondLogin.getId()).isEqualTo(1L);
    }
}
