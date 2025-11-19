package at.holly.socialloginstarter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA Entity representing an authenticated user from an OAuth2 provider.
 *
 * This demonstrates understanding of:
 * - Persisting OAuth2 user data to a database
 * - JPA entity mapping and annotations
 * - Unique constraints on provider + providerId combination
 * - Different data formats across providers (GitHub, Google, Facebook, etc.)
 * - The distinction between provider-specific ID and application user ID
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class User {

    /**
     * Application-specific user ID - auto-generated primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique ID from the OAuth2 provider (e.g., GitHub user ID: 12345)
     * Combined with provider to ensure uniqueness
     */
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    /**
     * Name of OAuth2 provider (github, google, facebook, etc.)
     * Combined with providerId to ensure uniqueness
     */
    @Column(nullable = false)
    private String provider;

    /**
     * User's email address from OAuth2 provider
     * May be null if user hasn't made it public
     */
    @Column
    private String email;

    /**
     * User's display name from OAuth2 provider
     */
    @Column
    private String name;

    /**
     * URL to user's avatar/profile picture from OAuth2 provider
     */
    @Column(length = 500)
    private String avatarUrl;

    /**
     * Timestamp when user first authenticated
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of user's most recent login
     * Updated on each authentication
     */
    @Column(nullable = false)
    private LocalDateTime lastLoginAt;

    /**
     * Raw OAuth2 provider attributes (not persisted to database)
     * Stored in memory during the session for display purposes
     * In production, you might serialize this to JSON and store in a TEXT column
     */
    @Transient
    private Map<String, Object> attributes;

    /**
     * Default constructor - required by JPA
     */
    public User() {
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    public User(String providerId, String provider, String email, String name, String avatarUrl, Map<String, Object> attributes) {
        this();
        this.providerId = providerId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.attributes = attributes;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", providerId='" + providerId + '\'' +
                ", provider='" + provider + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
