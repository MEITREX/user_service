package de.unistuttgart.iste.gits.user_service.controller;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.generated.dto.PublicUserInfo;
import de.unistuttgart.iste.gits.generated.dto.UserInfo;
import de.unistuttgart.iste.gits.user_service.service.UserService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
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
    List<PublicUserInfo> findPublicUserInfos(@Argument final List<UUID> ids) {
        return userService.findPublicUserInfos(ids);
    }

    @QueryMapping
    public UserInfo currentUserInfo(@ContextValue final LoggedInUser currentUser) {
        return new UserInfo(
                currentUser.getId(),
                currentUser.getUserName(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                LoggedInUser.RealmRole.getRoleStringsFromEnum(currentUser.getRealmRoles()).stream().toList()
        );
    }

    @QueryMapping
    public List<UserInfo> findUserInfos(@Argument final List<UUID> ids, final DataFetchingEnvironment env) {
        return userService.findUserInfos(ids, env);
    }
}
