package de.unistuttgart.iste.meitrex.user_service.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderConfiguration;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.AccessTokenEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.AccessTokenRepository;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


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
    public boolean isAccessTokenAvailable(LoggedInUser currentUser, ExternalServiceProviderDto providerDto) {
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
     * @param currentUserId ID of currently logged-in user.
     * @param providerDto   the external service provider name.
     * @return the active access token.
     * @throws EntityNotFoundException if no valid token is found.
     */
    public AccessToken getAccessToken(UUID currentUserId, ExternalServiceProviderDto providerDto) {
        final ExternalServiceProvider provider = modelMapper.map(providerDto, ExternalServiceProvider.class);

        final UserInfo currentUserInfo = userService.findUserInfo(currentUserId).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + currentUserId));
        final Optional<AccessTokenEntity> accessTokenOptional = accessTokenRepository.findByUserIdAndProvider(currentUserInfo.getId(), provider);

        if (accessTokenOptional.isEmpty()) {
            throw new EntityNotFoundException("Access token not found for user " + currentUserInfo.getId() + " and provider " + provider);
        }

        AccessTokenEntity accessToken = accessTokenOptional.get();
        OffsetDateTime now = OffsetDateTime.now();

        if (accessToken.getAccessTokenExpiresAt() == null || accessToken.getAccessTokenExpiresAt().isAfter(now)) {
            return new AccessToken(accessToken.getAccessToken(), accessToken.getExternalUserId());
        }

        if (!accessToken.getRefreshTokenExpiresAt().isAfter(now)) {
            throw new EntityNotFoundException("Access token expired and refresh token expired for user " + currentUserInfo.getId() + " and provider " + provider);
        }

        return refreshAccessToken(accessToken, provider);
    }

    private AccessToken refreshAccessToken(AccessTokenEntity accessToken, ExternalServiceProvider provider) {
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

                return new AccessToken(accessToken.getAccessToken(), accessToken.getExternalUserId());
            } else {
                log.error("Failed to refresh access token. HTTP Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
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
     * This method exchanges an authorization code for an access token.
     *
     * @param currentUser the currently logged-in user.
     * @param input       the input containing the authorization code and provider.
     * @return {@code true} if the access token was successfully generated and stored, otherwise {@code false}.
     */
    public boolean generateAccessToken(LoggedInUser currentUser, GenerateAccessTokenInput input) {
        final ExternalServiceProvider provider = modelMapper.map(input.getProvider(), ExternalServiceProvider.class);
        final UserInfo currentUserInfo = userService.findUserInfoInHeader(currentUser);
        final ExternalServiceProviderInfo providerInfo = providersConfig.getProviders().get(provider);

        try {
            AccessTokenResponse tokenResponse = exchangeCodeForAccessToken(input.getAuthorizationCode(), providerInfo);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                String externalUserId = this.tryGetExternalUserId(tokenResponse.getAccessToken(), providerInfo);

                AccessTokenEntity accessTokenEntity = AccessTokenEntity.builder()
                        .userId(currentUserInfo.getId())
                        .provider(provider)
                        .externalUserId(externalUserId)
                        .accessToken(tokenResponse.getAccessToken())
                        .accessTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresIn()))
                        .refreshToken(tokenResponse.getRefreshToken())
                        .refreshTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getRefreshTokenExpiresIn()))
                        .build();

                accessTokenRepository.save(accessTokenEntity);
                return tokenResponse.getAccessToken() != null;
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("Failed to generate access token for user {} and provider {}", currentUserInfo.getId(), provider, e);
        }
        return false;
    }

    private AccessTokenResponse exchangeCodeForAccessToken(String code, ExternalServiceProviderInfo providerInfo) throws IOException, InterruptedException {
        String requestBody = "client_id=" + providerInfo.getClientId() +
                "&client_secret=" + providerInfo.getClientSecret() +
                "&code=" + code;

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

    private String tryGetExternalUserId(String accessToken, ExternalServiceProviderInfo providerInfo) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(providerInfo.getExternalUserIdUrl()))
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

    public List<ExternalUserIdWithUser> getExternalUserIds(ExternalServiceProviderDto providerDto, List<UUID> userIds) {
        final ExternalServiceProvider provider = modelMapper.map(providerDto, ExternalServiceProvider.class);

        final List<ExternalUserIdWithUser> externalUserIds = new ArrayList<>();
        for (UUID userId : userIds) {
            final Optional<AccessTokenEntity> accessTokenOptional = accessTokenRepository.findByUserIdAndProvider(userId, provider);
            if (accessTokenOptional.isPresent()) {
                AccessTokenEntity accessToken = accessTokenOptional.get();
                externalUserIds.add(new ExternalUserIdWithUser(userId, accessToken.getExternalUserId()));
            }
        }
        return externalUserIds;
    }
}
