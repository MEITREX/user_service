package de.unistuttgart.iste.gits.user_service.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;

@RequiredArgsConstructor
public class KeycloakWrapper {
    private final RealmResource realm;

    public RealmResource getRealm() {
        return realm;
    }
}
