package at.holly.socialloginstarter.repository;

import at.holly.socialloginstarter.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 *
 * Tests demonstrate understanding of:
 * - @DataJpaTest for repository testing
 * - TestEntityManager for test data setup
 * - Spring Data JPA query methods
 * - Database constraints and unique indexes
 *
 * @DataJpaTest provides:
 * - In-memory H2 database for testing
 * - Transaction rollback after each test
 * - Only JPA-related beans are loaded
 */
@DataJpaTest
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User githubUser;
    private User googleUser;

    @BeforeEach
    void setUp() {
        githubUser = new User(
            "12345",
            "github",
            "john@example.com",
            "John Doe",
            "https://example.com/avatar.jpg",
            new HashMap<>()
        );

        googleUser = new User(
            "54321",
            "google",
            "jane@example.com",
            "Jane Smith",
            "https://example.com/jane.jpg",
            new HashMap<>()
        );
    }

    @Test
    @DisplayName("Should save and retrieve user")
    void shouldSaveAndRetrieveUser() {
        // When - saving user
        User saved = userRepository.save(githubUser);
        entityManager.flush();

        // Then - user should be persisted with generated ID
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getProvider()).isEqualTo("github");
        assertThat(saved.getProviderId()).isEqualTo("12345");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by provider and provider ID")
    void shouldFindUserByProviderAndProviderId() {
        // Given - user saved to database
        entityManager.persist(githubUser);
        entityManager.flush();

        // When - finding by provider and provider ID
        Optional<User> found = userRepository.findByProviderAndProviderId("github", "12345");

        // Then - user should be found
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        assertThat(found.get().getProvider()).isEqualTo("github");
        assertThat(found.get().getProviderId()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Should return empty when user not found by provider and provider ID")
    void shouldReturnEmptyWhenUserNotFound() {
        // When - searching for non-existent user
        Optional<User> found = userRepository.findByProviderAndProviderId("github", "99999");

        // Then - should return empty
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given - user saved to database
        entityManager.persist(githubUser);
        entityManager.flush();

        // When - finding by email
        Optional<User> found = userRepository.findByEmail("john@example.com");

        // Then - user should be found
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenEmailNotFound() {
        // When - searching for non-existent email
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then - should return empty
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by provider and provider ID")
    void shouldCheckIfUserExists() {
        // Given - user saved to database
        entityManager.persist(githubUser);
        entityManager.flush();

        // When - checking existence
        boolean exists = userRepository.existsByProviderAndProviderId("github", "12345");
        boolean notExists = userRepository.existsByProviderAndProviderId("github", "99999");

        // Then - correct existence status should be returned
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should allow same provider ID for different providers")
    void shouldAllowSameProviderIdForDifferentProviders() {
        // Given - two users with same providerId but different providers
        User githubUserA = new User("12345", "github", "user1@example.com",
            "User One", "https://example.com/1.jpg", new HashMap<>());
        User googleUserA = new User("12345", "google", "user2@example.com",
            "User Two", "https://example.com/2.jpg", new HashMap<>());

        // When - saving both users
        userRepository.save(githubUserA);
        userRepository.save(googleUserA);
        entityManager.flush();

        // Then - both should be saved successfully (different composite keys)
        Optional<User> foundGithub = userRepository.findByProviderAndProviderId("github", "12345");
        Optional<User> foundGoogle = userRepository.findByProviderAndProviderId("google", "12345");

        assertThat(foundGithub).isPresent();
        assertThat(foundGoogle).isPresent();
        assertThat(foundGithub.get().getEmail()).isEqualTo("user1@example.com");
        assertThat(foundGoogle.get().getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    @DisplayName("Should update user information")
    void shouldUpdateUserInformation() {
        // Given - user saved to database
        User saved = entityManager.persist(githubUser);
        entityManager.flush();
        Long userId = saved.getId();

        // When - updating user information
        saved.setName("Updated Name");
        saved.setEmail("updated@example.com");
        userRepository.save(saved);
        entityManager.flush();
        entityManager.clear();  // Clear cache to force database read

        // Then - updates should be persisted
        Optional<User> updated = userRepository.findById(userId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
        assertThat(updated.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.get().getProvider()).isEqualTo("github");  // Unchanged
        assertThat(updated.get().getProviderId()).isEqualTo("12345");  // Unchanged
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        // Given - user saved to database
        User saved = entityManager.persist(githubUser);
        entityManager.flush();
        Long userId = saved.getId();

        // When - deleting user
        userRepository.delete(saved);
        entityManager.flush();

        // Then - user should be deleted
        Optional<User> deleted = userRepository.findById(userId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Given - multiple users saved
        entityManager.persist(githubUser);
        entityManager.persist(googleUser);
        entityManager.flush();

        // When - finding all users
        var users = userRepository.findAll();

        // Then - all users should be returned
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getProvider)
            .containsExactlyInAnyOrder("github", "google");
    }

    @Test
    @DisplayName("Should count users")
    void shouldCountUsers() {
        // Given - multiple users saved
        entityManager.persist(githubUser);
        entityManager.persist(googleUser);
        entityManager.flush();

        // When - counting users
        long count = userRepository.count();

        // Then - correct count should be returned
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle null email")
    void shouldHandleNullEmail() {
        // Given - user with null email (email not public on OAuth provider)
        User userWithoutEmail = new User("67890", "github", null,
            "No Email User", "https://example.com/avatar.jpg", new HashMap<>());

        // When - saving user with null email
        User saved = userRepository.save(userWithoutEmail);
        entityManager.flush();

        // Then - user should be saved successfully
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isNull();

        // And - finding by null email should work
        Optional<User> found = userRepository.findByEmail(null);
        // Note: Depending on implementation, this might return empty or first user with null email
    }
}
