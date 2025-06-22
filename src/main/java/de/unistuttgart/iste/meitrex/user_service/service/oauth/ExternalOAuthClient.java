package de.unistuttgart.iste.meitrex.user_service.service.oauth;

import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Central client for handling OAuth2 operations by delegating to provider-specific strategies.
 * <p>
 * This component abstracts the logic for determining which {@link ExternalOAuthStrategy} to use
 * based on the {@link ExternalServiceProvider}, enabling support for multiple providers like GitHub.
 * </p>
 *
 * <p>Each method delegates to the appropriate strategy that supports the specified provider.</p>
 */
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

    /**
     * Delegates authorization code exchange to the provider-specific strategy.
     */
    public AccessTokenResponse exchangeCodeForAccessToken(String code, ExternalServiceProvider provider) throws IOException, InterruptedException {
        return getStrategy(provider).exchangeCodeForAccessToken(code);
    }

    /**
     * Delegates access token refresh to the provider-specific strategy.
     */
    public AccessTokenResponse refreshAccessToken(String refreshToken, ExternalServiceProvider provider) throws IOException, InterruptedException {
        return getStrategy(provider).refreshAccessToken(refreshToken);
    }

    /**
     * Delegates external user ID fetch to the provider-specific strategy.
     */
    public String fetchExternalUserId(String accessToken, ExternalServiceProvider provider) throws IOException, InterruptedException {
        return getStrategy(provider).fetchExternalUserId(accessToken);
    }
}
