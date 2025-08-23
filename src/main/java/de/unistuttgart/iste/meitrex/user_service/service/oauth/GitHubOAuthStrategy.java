package de.unistuttgart.iste.meitrex.user_service.service.oauth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderConfiguration;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * OAuth2 strategy implementation for GitHub.
 * <p>
 * This class encapsulates all provider-specific logic required to interact with GitHub’s OAuth2 API,
 * including:
 * <ul>
 *     <li>Exchanging authorization codes for access tokens</li>
 *     <li>Refreshing expired access tokens (if enabled)</li>
 *     <li>Fetching the authenticated user's GitHub username</li>
 * </ul>
 * <p>
 * Configuration details (such as client credentials and endpoint URLs) are retrieved from
 * {@link ExternalServiceProviderConfiguration}, and must be defined under the
 * {@code thirdparty.providers.github} prefix in {@code application.properties}.
 * </p>
 *
 * <h3>Example usage:</h3>
 * This strategy is selected by {@link ExternalOAuthClient} based on the
 * {@link ExternalServiceProvider#GITHUB} enum value.
 *
 * <h3>Example configuration:</h3>
 * <pre>
 * thirdparty.providers.github.clientId=...
 * thirdparty.providers.github.clientSecret=...
 * thirdparty.providers.github.tokenRequestUrl=https://github.com/login/oauth/access_token
 * thirdparty.providers.github.externalUserIdUrl=https://api.github.com/user
 * </pre>
 *
 * @see ExternalOAuthStrategy
 * @see ExternalOAuthClient
 * @see ExternalServiceProviderConfiguration
 * @see ExternalServiceProviderInfo
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubOAuthStrategy implements ExternalOAuthStrategy {

    private final HttpClient client;

    private final ExternalServiceProviderConfiguration providersConfig;

    private ExternalServiceProviderInfo githubInfo() {
        return providersConfig.getProviders().get(ExternalServiceProvider.GITHUB);
    }


    @Override
    public boolean supports(ExternalServiceProvider providerName) {
        return providerName.equals(ExternalServiceProvider.GITHUB);
    }

    @Override
    public AccessTokenResponse exchangeCodeForAccessToken(String code) throws IOException, InterruptedException {
        String requestBody = "client_id=" + githubInfo().getClientId() +
                "&client_secret=" + githubInfo().getClientSecret() +
                "&code=" + code;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(githubInfo().getTokenRequestUrl()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Failed to exchange code. HTTP {} Body: {}", response.statusCode(), response.body());
            return null;
        }

        try {
            return parseTokenResponse(response.body());
        } catch (Exception ex) {
            log.error("Non-JSON or malformed token response body: {}", response.body());
            return null;
        }
    }

    @Override
    public AccessTokenResponse refreshAccessToken(String refreshToken) throws IOException, InterruptedException {
        String requestBody = "client_id=" + githubInfo().getClientId() +
                "&client_secret=" + githubInfo().getClientSecret() +
                "&grant_type=refresh_token" +
                "&refresh_token=" + refreshToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(githubInfo().getTokenRequestUrl()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Failed to refresh token. HTTP {} Body: {}", response.statusCode(), response.body());
            return null;
        }

        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("error")) {
                log.error("GitHub refresh error: {}", json);
                return null;
            }
            return parseTokenResponse(response.body());
        } catch (Exception ex) {
            log.error("Non-JSON or malformed refresh response body: {}", response.body());
            return null;
        }
    }


    public String fetchExternalUserId(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(githubInfo().getExternalUserIdUrl()))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonObject user = JsonParser.parseString(response.body()).getAsJsonObject();
            return user.get("login").getAsString();
        }

        return null;
    }

    private AccessTokenResponse parseTokenResponse(String responseBody) {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        // If access token is non-expiring, there are no expires_in, refresh_token, or refresh_token_expires_in fields in the response body
        return AccessTokenResponse.builder()
                .accessToken(jsonResponse.get("access_token").getAsString())
                .expiresIn(jsonResponse.has("expires_in") ? jsonResponse.get("expires_in").getAsInt() : null)
                .refreshToken(jsonResponse.has("refresh_token") ? jsonResponse.get("refresh_token").getAsString() : null)
                .refreshTokenExpiresIn(jsonResponse.has("refresh_token_expires_in") ? jsonResponse.get("refresh_token_expires_in").getAsInt() : null)
                .build();
    }
}
