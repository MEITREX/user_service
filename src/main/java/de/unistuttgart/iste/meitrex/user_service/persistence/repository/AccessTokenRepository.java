package de.unistuttgart.iste.meitrex.user_service.persistence.repository;

import de.unistuttgart.iste.meitrex.user_service.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessTokenEntity, AccessTokenPk> {
    /**
     * Finds a third-party access token entity by the user ID and provider.
     *
     * @param userId   The UUID of the user.
     * @param provider The third-party provider (e.g., GITHUB).
     * @return An {@link Optional} containing the {@link AccessTokenEntity} if found,
     *         otherwise an empty {@link Optional}.
     */
    Optional<AccessTokenEntity> findByUserIdAndProvider(UUID userId, ExternalServiceProvider provider);
}
