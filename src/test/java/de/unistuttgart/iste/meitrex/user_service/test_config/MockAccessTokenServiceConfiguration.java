package de.unistuttgart.iste.meitrex.user_service.test_config;

import de.unistuttgart.iste.meitrex.user_service.service.AccessTokenService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockAccessTokenServiceConfiguration {

    @Primary
    @Bean
    public AccessTokenService accessTokenService() {
        return Mockito.mock(AccessTokenService.class);
    }
}
