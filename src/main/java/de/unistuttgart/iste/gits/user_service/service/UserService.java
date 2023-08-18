package de.unistuttgart.iste.gits.user_service.service;

import de.unistuttgart.iste.gits.generated.dto.PublicUserInfo;
import de.unistuttgart.iste.gits.generated.dto.UserInfo;
import de.unistuttgart.iste.gits.user_service.config.KeycloakWrapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final KeycloakWrapper keycloak;

    /**
     * Find public user infos by ids.
     * If a user is not found, the corresponding entry in the list will be null.
     *
     * @param ids List of user ids
     * @return List of (nullable) public user infos
     */
    public List<PublicUserInfo> findPublicUserInfos(List<UUID> ids) {
        return ids.stream()
                .map(this::findPublicUserInfo)
                .map(optional -> optional.orElse(null))
                .toList();
    }

    /**
     * Find public user info by id.
     *
     * @param id User id
     * @return an optional of the public user info or empty if the user could not be retrieved.
     */
    public Optional<PublicUserInfo> findPublicUserInfo(UUID id) {
        return findUser(id).map(user -> new PublicUserInfo(id, user.getUsername()));
    }

    /**
     * Find user infos by ids.
     * If a user is not found, the corresponding entry in the list will be null.
     *
     * @param ids List of user ids
     * @return List of (nullable) user infos
     */
    public List<UserInfo> findUserInfos(List<UUID> ids) {
        return ids.stream()
                .map(this::findUserInfo)
                .map(optional -> optional.orElse(null))
                .toList();
    }

    /**
     * Find user info by id.
     *
     * @param id User id
     * @return an optional of the user info or empty if the user could not be retrieved.
     */
    public Optional<UserInfo> findUserInfo(UUID id) {
        return findUser(id)
                .map(user -> new UserInfo(
                        id,
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        List.of() // course memberships are resolved with schema mapping
                ));
    }

    /**
     * Get user info by id.
     *
     * @param id User id
     * @return The user info
     * @throws EntityNotFoundException if the user could not be retrieved.
     */
    public UserInfo getUserInfo(UUID id) {
        return findUserInfo(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    private Optional<UserRepresentation> findUser(UUID id) {
        try {
            return Optional.of(keycloak.getRealm().users().get(id.toString()).toRepresentation());
        } catch (Exception e) {
            log.error("User not found", e);
            return Optional.empty();
        }
    }
}
