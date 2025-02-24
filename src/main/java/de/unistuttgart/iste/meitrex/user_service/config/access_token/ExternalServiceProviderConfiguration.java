package de.unistuttgart.iste.meitrex.user_service.config.access_token;

import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "thirdparty")
public class ExternalServiceProviderConfiguration {
    private Map<ExternalServiceProvider, ExternalServiceProviderInfo> providers;
}
