package de.unistuttgart.iste.meitrex.user_service.service.oauth;

import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;

import java.io.IOException;

public interface ExternalOAuthStrategy {

    boolean supports(ExternalServiceProvider providerName);

    AccessTokenResponse exchangeCodeForAccessToken(String code) throws IOException, InterruptedException;

    AccessTokenResponse refreshAccessToken(String refreshToken) throws IOException, InterruptedException;

    String fetchExternalUserId(String accessToken) throws IOException, InterruptedException;
}
