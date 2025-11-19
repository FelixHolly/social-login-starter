package at.holly.socialloginstarter.repository;

import at.holly.socialloginstarter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for User entity.
 *
 * This demonstrates understanding of:
 * - Spring Data JPA repository pattern
 * - Custom query methods using method naming conventions
 * - Finding users by composite natural key (provider + providerId)
 * - Optional return types for null safety
 *
 * Spring Data JPA automatically implements this interface at runtime,
 * providing CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their OAuth2 provider and provider-specific ID.
     *
     * This is the key lookup for OAuth2 authentication:
     * - When a user logs in via GitHub with ID "12345", we search for provider="github" and providerId="12345"
     * - If found, we update their info (name, email, lastLoginAt)
     * - If not found, we create a new user record
     *
     * Spring Data JPA automatically generates the query:
     * SELECT * FROM users WHERE provider = ? AND provider_id = ?
     *
     * @param provider The OAuth2 provider name (github, google, facebook, etc.)
     * @param providerId The user's unique ID at that provider
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * Finds a user by their email address.
     *
     * Useful for:
     * - Account linking (same email across multiple providers)
     * - Email-based user lookups
     * - Detecting duplicate accounts
     *
     * Note: Email may be null if user hasn't made it public on OAuth2 provider
     *
     * @param email The user's email address
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists for a given provider and provider ID.
     *
     * More efficient than findByProviderAndProviderId when you only need
     * to check existence without loading the entity.
     *
     * @param provider The OAuth2 provider name
     * @param providerId The user's unique ID at that provider
     * @return true if user exists, false otherwise
     */
    boolean existsByProviderAndProviderId(String provider, String providerId);

    /*
     * INHERITED METHODS from JpaRepository<User, Long>:
     *
     * save(User user) - Save or update a user
     * findById(Long id) - Find user by ID
     * findAll() - Get all users
     * delete(User user) - Delete a user
     * count() - Count total users
     * ... and many more
     *
     * These are automatically implemented by Spring Data JPA.
     */

    /*
     * PRODUCTION CONSIDERATIONS:
     *
     * For a production application, you might add:
     *
     * 1. PAGINATION AND SORTING
     *    Page<User> findByProvider(String provider, Pageable pageable);
     *
     * 2. CUSTOM QUERIES
     *    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date")
     *    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
     *
     * 3. PROJECTION INTERFACES
     *    interface UserSummary {
     *        String getName();
     *        String getEmail();
     *    }
     *    List<UserSummary> findByProvider(String provider);
     *
     * 4. BATCH OPERATIONS
     *    @Modifying
     *    @Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.id IN :ids")
     *    int updateLastLoginBatch(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);
     */
}
