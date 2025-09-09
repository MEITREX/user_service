package de.unistuttgart.iste.meitrex.user_service.config.user;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;

@RequiredArgsConstructor
public class KeycloakWrapper {
    private final RealmResource realm;

    public RealmResource getRealm() {
        return realm;
    }
}
