package de.unistuttgart.iste.gits.user_service.controller;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.generated.dto.PublicUserInfo;
import de.unistuttgart.iste.gits.generated.dto.UserInfo;
import de.unistuttgart.iste.gits.user_service.config.KeycloakWrapper;
import de.unistuttgart.iste.gits.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @QueryMapping
    public PublicUserInfo publicUserInfo(@Argument UUID id) {
        return userService.getPublicUserInfo(id);
    }

    @QueryMapping
    List<PublicUserInfo> publicUserInfoBatched(@Argument List<UUID> ids) {
        return ids.stream().map(this::publicUserInfo).toList();
    }

    @QueryMapping
    public UserInfo currentUserInfo(@ContextValue LoggedInUser currentUser) {
        return userService.getUserInfo(currentUser.getId());
    }

    @QueryMapping
    public UserInfo userInfo(@Argument UUID id) {
        return userService.getUserInfo(id);
    }

    @QueryMapping
    public List<UserInfo> userInfoBatched(@Argument List<UUID> ids) {
        return ids.stream().map(this::userInfo).toList();
    }
}
