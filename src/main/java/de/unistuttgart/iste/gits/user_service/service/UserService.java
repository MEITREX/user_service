package de.unistuttgart.iste.gits.user_service.service;

import de.unistuttgart.iste.gits.generated.dto.PublicUserInfo;
import de.unistuttgart.iste.gits.generated.dto.UserInfo;
import de.unistuttgart.iste.gits.user_service.config.KeycloakWrapper;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final KeycloakWrapper keycloak;

    public PublicUserInfo getPublicUserInfo(UUID id) {
        UserRepresentation user = keycloak.getRealm().users().get(id.toString()).toRepresentation();
        return new PublicUserInfo(
                id,
                user.getUsername()
        );
    }

    public UserInfo getUserInfo(UUID id) {
        UserRepresentation user = keycloak.getRealm().users().get(id.toString()).toRepresentation();
        return new UserInfo(
                id,
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
