package de.unistuttgart.iste.meitrex.user_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.PublicUserInfo;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.service.UserService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @QueryMapping
    List<PublicUserInfo> findPublicUserInfos(@Argument List<UUID> ids) {
        return userService.findPublicUserInfos(ids);
    }

    @QueryMapping
    public UserInfo currentUserInfo(@ContextValue LoggedInUser currentUser) {
        return userService.findUserInfoInHeader(currentUser);
    }

    @QueryMapping
    public List<UserInfo> findUserInfos(@Argument List<UUID> ids, DataFetchingEnvironment env) {
        return userService.findUserInfos(ids, env);
    }

    @QueryMapping(name="_internal_noauth_userNicknameByUserId")
    public UserInfo getUserInfo(@Argument UUID userId) {
        return userService.findUserInfo(userId).orElseThrow(() ->
                new EntityNotFoundException("Could not find user with id " + userId));
    }

    @MutationMapping
    public UserInfo setNickname(@Argument String nickname,
        @ContextValue LoggedInUser currentUser) {
        return userService.setNickname(currentUser.getId(), nickname);
    }
}
