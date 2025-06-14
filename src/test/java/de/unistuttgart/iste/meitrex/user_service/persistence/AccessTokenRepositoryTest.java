package de.unistuttgart.iste.meitrex.user_service.persistence;

import de.unistuttgart.iste.meitrex.user_service.persistence.entity.AccessTokenEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.AccessTokenRepository;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest({"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(classes = MockKeycloakConfiguration.class)
class AccessTokenRepositoryTest {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Test

    void testFindTokenByUserIdAndProvider() {
        UUID userId = UUID.randomUUID();
        AccessTokenEntity accessToken = AccessTokenEntity.builder()
                .userId(userId)
                .provider(ExternalServiceProvider.GITHUB)
                .accessToken("token")
                .accessTokenExpiresAt(OffsetDateTime.now())
                .build();

        accessTokenRepository.save(accessToken);

        Optional<AccessTokenEntity> retrievedToken = accessTokenRepository.findByUserIdAndProvider(userId, ExternalServiceProvider.GITHUB);

        assertThat(retrievedToken.isPresent(), is(true));
        assertThat(retrievedToken.get().getUserId(), is(userId));
    }
}