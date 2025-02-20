package de.unistuttgart.iste.meitrex.user_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents an access token entity for storing authentication tokens
 * associated with a user and an external service provider (e.g., GitHub).
 * <p>
 * This entity maintains access and refresh tokens, along with their expiration times.
 * The combination of {@code userId} and {@code provider} serves as the primary key.
 * </p>
 */
@Entity(name = "AccessToken")
@IdClass(AccessTokenPk.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenEntity {
    @Id
    @Column(nullable = false)
    private UUID userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExternalServiceProvider provider;

    /**
     * The access token used for authentication with the external service provider.
     * <p>
     * This token is required for making authenticated API requests on behalf of the user.
     * It has a maximum length of 4096 characters and can expire after a set duration.
     * Once expired, a new access token can be obtained using a refresh token (if available).
     * </p>
     */
    @Column(nullable = false, length = 4096)
    private String accessToken;


    /**
     * If available the refresh token is used to obtain a new access token when it expires.
     * <p>
     * The token is optional and stored as a string with a maximum length of 4096 characters.
     * </p>
     */
    @Column(length = 4096)
    private String refreshToken;

    @Column(nullable = false)
    private OffsetDateTime accessTokenExpiresAt;

    @Column(updatable = false)
    private OffsetDateTime refreshTokenExpiresAt;
}
