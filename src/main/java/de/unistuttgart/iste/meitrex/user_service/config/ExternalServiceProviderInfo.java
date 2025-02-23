package de.unistuttgart.iste.meitrex.user_service.config;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Configuration class for an external service provider's OAuth2 authentication details.
 * This class holds credentials and endpoint URLs required for obtaining and refreshing access tokens.
 */
@Data
@Builder
public class ExternalServiceProviderInfo {
    @NonNull
    private final String clientId;

    @NonNull
    private final String clientSecret;

    @NonNull
    private final String tokenRequestUrl;
}

