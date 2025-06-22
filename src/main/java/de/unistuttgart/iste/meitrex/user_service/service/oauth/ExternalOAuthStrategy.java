package de.unistuttgart.iste.meitrex.user_service.service.oauth;

import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;

import java.io.IOException;

/**
 * Interface representing an OAuth2 strategy for a specific external service provider.
 * <p>
 * Implementations of this interface encapsulate provider-specific behavior
 * for handling OAuth2 flows, such as GitHub etc.
 * </p>
 *
 * <p>This strategy interface enables support for multiple providers using a plug-and-play pattern,
 * ensuring the application remains scalable and maintainable.</p>
 *
 * <p>Implemented by classes like {@link de.unistuttgart.iste.meitrex.user_service.service.oauth.GitHubOAuthStrategy}.</p>
 */
public interface ExternalOAuthStrategy {

    /**
     * Checks if this strategy supports a given external service provider.
     *
     * @param providerName the provider to check, e.g., GITHUB.
     * @return {@code true} if this strategy supports the provider, otherwise {@code false}.
     */
    boolean supports(ExternalServiceProvider providerName);

    /**
     * Exchanges the authorization code for an access token using the provider's token endpoint.
     *
     * @param code the authorization code received from the provider after user authorization.
     * @return {@link AccessTokenResponse} containing access token and optionally refresh token.
     * @throws IOException if a network or JSON parsing error occurs.
     * @throws InterruptedException if the thread is interrupted during the request.
     */
    AccessTokenResponse exchangeCodeForAccessToken(String code) throws IOException, InterruptedException;

    /**
     * Refreshes an access token using the given refresh token.
     *
     * @param refreshToken the refresh token previously received from the provider.
     * @return {@link AccessTokenResponse} with new access and refresh token details.
     * @throws IOException if a network or JSON parsing error occurs.
     * @throws InterruptedException if the thread is interrupted during the request.
     */
    AccessTokenResponse refreshAccessToken(String refreshToken) throws IOException, InterruptedException;

    /**
     * Retrieves the external user ID (e.g., GitHub username) from the provider using the access token.
     *
     * @param accessToken the access token to authenticate the request.
     * @return the external user ID as a {@link String}, or {@code null} if the request fails.
     * @throws IOException if a network or JSON parsing error occurs.
     * @throws InterruptedException if the thread is interrupted during the request.
     */
    String fetchExternalUserId(String accessToken) throws IOException, InterruptedException;
}
