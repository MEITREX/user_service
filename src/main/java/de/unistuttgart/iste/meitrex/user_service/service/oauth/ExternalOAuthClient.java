package de.unistuttgart.iste.meitrex.user_service.service.oauth;

import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExternalOAuthClient {

    private final List<ExternalOAuthStrategy> strategies;

    private ExternalOAuthStrategy getStrategy(ExternalServiceProvider providerName) {
        return strategies.stream()
                .filter(s -> s.supports(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OAuth strategy found for provider: " + providerName));
    }

    public AccessTokenResponse exchangeCodeForAccessToken(String code, ExternalServiceProvider provider) throws IOException, InterruptedException {
        return getStrategy(provider).exchangeCodeForAccessToken(code);
    }

    public AccessTokenResponse refreshAccessToken(String refreshToken, ExternalServiceProvider provider) throws IOException, InterruptedException {
        return getStrategy(provider).refreshAccessToken(refreshToken);
    }

    public String fetchExternalUserId(String accessToken, ExternalServiceProvider provider) throws IOException, InterruptedException {
        return getStrategy(provider).fetchExternalUserId(accessToken);
    }
}
