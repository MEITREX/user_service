package de.unistuttgart.iste.gits.user_service.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfiguration {

    private final Environment env;

    @Bean
    public KeycloakWrapper keycloak() {
        Keycloak keycloak = Keycloak.getInstance(
                env.getProperty("keycloak.url"),
                env.getProperty("keycloak.masterRealm"),
                env.getProperty("keycloak.username"),
                env.getProperty("keycloak.password"),
                env.getProperty("keycloak.clientId")
        );

        return new KeycloakWrapper(keycloak.realm(env.getProperty("keycloak.realm")));
    }
}
