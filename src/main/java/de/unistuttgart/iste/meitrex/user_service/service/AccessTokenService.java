package de.unistuttgart.iste.meitrex.user_service.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.GenerateAccessTokenInput;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderConfiguration;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.AccessTokenEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.AccessTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.Optional;


/**
 * Service class responsible for handling OAuth2 access tokens for external service providers.
 * This includes checking token availability, retrieving stored tokens, refreshing expired tokens,
 * and generating new tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessTokenService {

    private final UserService userService;

    private final AccessTokenRepository accessTokenRepository;

    private final ExternalServiceProviderConfiguration providersConfig;

    private final ModelMapper modelMapper;

    private final HttpClient client;

    /**
     * Checks if a valid access token or refresh token is available for a given user and provider.
     *
     * @param currentUser the currently logged-in user.
     * @param providerDto the external service provider name.
     * @return {@code true} if a valid token exists, otherwise {@code false}.
     */
    public Boolean isAccessTokenAvailable(LoggedInUser currentUser, ExternalServiceProviderDto providerDto) {
        final ExternalServiceProvider provider = modelMapper.map(providerDto, ExternalServiceProvider.class);

        final UserInfo currentUserInfo = userService.findUserInfoInHeader(currentUser);
        final Optional<AccessTokenEntity> accessTokenOptional = accessTokenRepository.findByUserIdAndProvider(currentUserInfo.getId(), provider);

        if (accessTokenOptional.isEmpty()) {
            return false;
        }

        AccessTokenEntity accessToken = accessTokenOptional.get();
        OffsetDateTime now = OffsetDateTime.now();

        // Access tokens can be non-expiring
        if (accessToken.getAccessTokenExpiresAt() == null || accessToken.getAccessTokenExpiresAt().isAfter(now)) {
            return true;
        }

        // If access token expired, check if the refresh token is expired
        return accessToken.getRefreshTokenExpiresAt().isAfter(now);
    }

    /**
     * Retrieves the access token for a given user and provider.
     * If the token is expired and a valid refresh token exists, the token is refreshed.
     *
     * @param currentUser the currently logged-in user.
     * @param providerDto the external service provider name.
     * @return the active access token.
     * @throws EntityNotFoundException if no valid token is found.
     */
    public String getAccessToken(LoggedInUser currentUser, ExternalServiceProviderDto providerDto) {
        final ExternalServiceProvider provider = modelMapper.map(providerDto, ExternalServiceProvider.class);

        final UserInfo currentUserInfo = userService.findUserInfoInHeader(currentUser);
        final Optional<AccessTokenEntity> accessTokenOptional = accessTokenRepository.findByUserIdAndProvider(currentUserInfo.getId(), provider);

        if (accessTokenOptional.isEmpty()) {
            throw new EntityNotFoundException("Access token not found for user " + currentUserInfo.getId() + " and provider " + provider);
        }

        AccessTokenEntity accessToken = accessTokenOptional.get();
        OffsetDateTime now = OffsetDateTime.now();

        if (accessToken.getAccessTokenExpiresAt() == null || accessToken.getAccessTokenExpiresAt().isAfter(now)) {
            return accessToken.getAccessToken();
        }

        if (!accessToken.getRefreshTokenExpiresAt().isAfter(now)) {
            throw new EntityNotFoundException("Access token expired and refresh token expired for user " + currentUserInfo.getId() + " and provider " + provider);
        }

        return refreshAccessToken(accessToken, provider);
    }

    private String refreshAccessToken(AccessTokenEntity accessToken, ExternalServiceProvider provider) {
        try {
            ExternalServiceProviderInfo providerInfo = providersConfig.getProviders().get(provider);

            String requestBody = "client_id=" + providerInfo.getClientId() +
                    "&client_secret=" + providerInfo.getClientSecret() +
                    "&grant_type=refresh_token" +
                    "&refresh_token=" + accessToken.getRefreshToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(providerInfo.getTokenRequestUrl()))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AccessTokenResponse tokenResponse = parseTokenResponse(response.body());

                accessToken.setAccessToken(tokenResponse.getAccessToken());
                accessToken.setAccessTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
                accessToken.setRefreshToken(tokenResponse.getRefreshToken());
                accessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getRefreshTokenExpiresIn()));

                accessTokenRepository.save(accessToken);

                return tokenResponse.getAccessToken();
            } else {
                log.error("Failed to refresh access token. HTTP Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to refresh access token for user {} and provider {}", accessToken.getUserId(), provider, e);
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

    /**
     * Generates a new access token for the given user and external service provider.
     * This method exchanges an authorization code for an access token and stores it in the database.
     *
     * @param currentUser the currently logged-in user.
     * @param input       the input containing the authorization code, provider, and redirect URI.
     * @return {@code true} if the access token was successfully generated and stored, otherwise {@code false}.
     */
    public Boolean generateAccessToken(LoggedInUser currentUser, GenerateAccessTokenInput input) {
        final ExternalServiceProvider provider = modelMapper.map(input.getProvider(), ExternalServiceProvider.class);
        final UserInfo currentUserInfo = userService.findUserInfoInHeader(currentUser);

        try {
            AccessTokenResponse tokenResponse = exchangeCodeForAccessToken(provider, input.getAuthorizationCode(), input.getRedirectUri());

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                AccessTokenEntity accessTokenEntity = AccessTokenEntity.builder()
                        .userId(currentUserInfo.getId())
                        .provider(provider)
                        .accessToken(tokenResponse.getAccessToken())
                        .accessTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresIn()))
                        .refreshToken(tokenResponse.getRefreshToken())
                        .refreshTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getRefreshTokenExpiresIn()))
                        .build();

                accessTokenRepository.save(accessTokenEntity);
                return tokenResponse.getAccessToken() != null;
            }
        } catch (Exception e) {
            log.error("Failed to generate access token for user {} and provider {}", currentUserInfo.getId(), provider, e);
        }

        return false;
    }

    private AccessTokenResponse exchangeCodeForAccessToken(ExternalServiceProvider provider, String code, String redirectUri) throws IOException, InterruptedException {
        ExternalServiceProviderInfo providerInfo = providersConfig.getProviders().get(provider);

        String requestBody = "client_id=" + providerInfo.getClientId() +
                "&client_secret=" + providerInfo.getClientSecret() +
                "&code=" + code +
                "&redirect_uri=" + redirectUri;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(providerInfo.getTokenRequestUrl()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseTokenResponse(response.body());
        }

        log.error("Failed to exchange code for access token. HTTP Status: {} Response: {}", response.statusCode(), response.body());
        return null;
    }

    /**
     * DTO representing an OAuth2 access token response.
     * Used to encapsulate token details received from an external service provider.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessTokenResponse {
        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private Integer refreshTokenExpiresIn;
    }
}
