package de.unistuttgart.iste.gits.user_service.service;

import de.unistuttgart.iste.gits.generated.dto.PublicUserInfo;
import de.unistuttgart.iste.gits.generated.dto.UserInfo;
import de.unistuttgart.iste.gits.user_service.config.KeycloakWrapper;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
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
    public List<UserInfo> findUserInfos(List<UUID> ids, DataFetchingEnvironment env) {
        // the selection set also contains fields of children, so we first need to filter only the direct child fields
        // of the user info type
        List<SelectedField> userInfoSubfields = env.getSelectionSet().getFields().stream()
                .filter(field -> field.getObjectTypeNames().contains("UserInfo"))
                .toList();

        // if we're not requesting any fields except courseMemberships, we can skip the fetching of user data because
        // the data is never used by graphql (course membership data is handled in its own schema mapping)
        if(userInfoSubfields.stream().filter(field -> field.getObjectTypeNames().contains("UserInfo")).count() == 1
                && userInfoSubfields.get(0).getName().equals("courseMemberships")) {
            // just return a list of empty user infos, just set their id and nothing else
            return ids.stream().map(x -> {
                UserInfo user = new UserInfo();
                user.setId(x);
                return user;
            }).toList();
        }

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
