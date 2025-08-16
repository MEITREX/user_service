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
import de.unistuttgart.iste.meitrex.user_service.service.oauth.ExternalOAuthClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private final ModelMapper modelMapper;

    private final ExternalOAuthClient externalOAuthClient;

    /**
     * Checks if a valid access token is available for a given user and provider.
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

        // If access token expired, check if the refresh token is expired and try to refresh it
        // There's a bug in GH Api that returns 200 error response because it thinks there is a problem with the refresh token (there is not), so we check here if the refresh works.
        // if it does, then refreshAccessToken returns true and a new access token is saved, so getAccessToken can return it (without needing to refresh).
        // if it doesn't, then returns false and the user will be prompted to re-authorize GitHub. (using generateAccessToken)
        return accessToken.getRefreshTokenExpiresAt().isAfter(now) && refreshAccessToken(accessToken, provider) != null;
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

        // the lines below won't run in current setup, since if we call getAccessToken, we already refreshed the access token if needed in isAccessTokenAvailable.
        // but if Github resolve their problem, the normal workflow should be used instead, i.e. in isAccessTokenAvailable, we only check if the access token is expired without refreshing.
        // The refresh will be done below then (check the isAccessTokenAvailable method for more details)
        if (!accessToken.getRefreshTokenExpiresAt().isAfter(now)) {
            throw new EntityNotFoundException("Access token expired and refresh token expired for user " + currentUserInfo.getId() + " and provider " + provider);
        }

        return refreshAccessToken(accessToken, provider);
    }

    private AccessToken refreshAccessToken(AccessTokenEntity accessToken, ExternalServiceProvider provider) {
        try {
            AccessTokenResponse tokenResponse = externalOAuthClient.refreshAccessToken(accessToken.getRefreshToken(), provider);

            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                log.error("Failed to refresh access token for user {} and provider {}", accessToken.getUserId(), provider);
                return null;
            }

            accessToken.setAccessToken(tokenResponse.getAccessToken());
            accessToken.setAccessTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            accessToken.setRefreshToken(tokenResponse.getRefreshToken());
            accessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getRefreshTokenExpiresIn()));

            accessTokenRepository.save(accessToken);
            return new AccessToken(accessToken.getAccessToken(), accessToken.getExternalUserId());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("Failed to refresh access token for user {} and provider {}", accessToken.getUserId(), provider, e);
        }
        return null;
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

        try {
            AccessTokenResponse tokenResponse = externalOAuthClient.exchangeCodeForAccessToken(input.getAuthorizationCode(), provider);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                String externalUserId = externalOAuthClient.fetchExternalUserId(tokenResponse.getAccessToken(), provider);

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
