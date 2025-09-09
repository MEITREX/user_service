package de.unistuttgart.iste.meitrex.user_service.config.access_token;

import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration holder for OAuth2 credentials and endpoints of third-party service providers.
 * <p>
 * This class is populated from application properties using the {@code thirdparty.providers} prefix.
 * Each {@link ExternalServiceProvider} maps to a set of credentials and endpoint URLs defined in
 * {@link ExternalServiceProviderInfo}.
 * </p>
 *
 * <h3>Example configuration (in application.properties):</h3>
 * <pre>
 * thirdparty.providers.github.clientId=fefw2HFfnk2
 * thirdparty.providers.github.clientSecret=a9d1frewffrhg3a9d1frewffrhg3
 * thirdparty.providers.github.tokenRequestUrl=https://github.com/login/oauth/access_token
 * thirdparty.providers.github.externalUserIdUrl=https://api.github.com/user
 * </pre>
 *
 * <p><strong>Note:</strong> Each provider key (e.g. {@code github}) must match the name of a value
 * from the {@link ExternalServiceProvider} enum.</p>
 *
 * @see ExternalServiceProviderInfo
 * @see ExternalServiceProvider
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "thirdparty")
public class ExternalServiceProviderConfiguration {
    private Map<ExternalServiceProvider, ExternalServiceProviderInfo> providers;
}
