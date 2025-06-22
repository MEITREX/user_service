package de.unistuttgart.iste.meitrex.user_service.test_config;

import de.unistuttgart.iste.meitrex.generated.dto.AccessToken;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalUserIdWithUser;
import de.unistuttgart.iste.meitrex.user_service.service.AccessTokenService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@TestConfiguration
public class MockAccessTokenServiceConfiguration {

    @Primary
    @Bean
    public AccessTokenService accessTokenService() {
        AccessTokenService mock = Mockito.mock(AccessTokenService.class);

        Mockito.when(mock.getAccessToken(any(), eq(ExternalServiceProviderDto.GITHUB)))
                .thenReturn(new AccessToken("mocked-access-token", null));

        Mockito.when(mock.getExternalUserIds(eq(ExternalServiceProviderDto.GITHUB), any()))
                .thenAnswer(invocation -> {
                    List<UUID> userIds = invocation.getArgument(1);
                    return userIds.stream()
                            .map(id -> {
                                String externalId;
                                if (MockKeycloakConfiguration.firstUserId.equals(id)) {
                                    externalId = "github_firstuser";
                                } else if (MockKeycloakConfiguration.secondUserId.equals(id)) {
                                    externalId = "github_seconduser";
                                } else if (MockKeycloakConfiguration.thirdUserId.equals(id)) {
                                    externalId = "github_thirduser";
                                } else {
                                    externalId = null; // simulate missing mapping
                                }
                                return new ExternalUserIdWithUser(id, externalId);
                            }).toList();
                });

        return mock;
    }
}

