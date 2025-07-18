package de.unistuttgart.iste.meitrex.user_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalUserIdWithUser;
import de.unistuttgart.iste.meitrex.generated.dto.GenerateAccessTokenInput;
import de.unistuttgart.iste.meitrex.user_service.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import de.unistuttgart.iste.meitrex.generated.dto.AccessToken;
import lombok.extern.java.Log;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourse;

@Controller
@RequiredArgsConstructor
public class AccessTokenController {

    private final AccessTokenService accessTokenService;

    @QueryMapping
    public boolean isAccessTokenAvailable(@ContextValue LoggedInUser currentUser, @Argument ExternalServiceProviderDto provider) {
        return accessTokenService.isAccessTokenAvailable(currentUser, provider);
    }

    @QueryMapping(name="_internal_noauth_getAccessToken")
    public AccessToken getAccessToken(@Argument UUID userId, @Argument ExternalServiceProviderDto provider) {
        return accessTokenService.getAccessToken(userId, provider);
    }

    @QueryMapping(name="_internal_noauth_getExternalUserIds")
    public List<ExternalUserIdWithUser> getExternalUserIds(@Argument ExternalServiceProviderDto provider, @Argument List<UUID> userIds) {
        return accessTokenService.getExternalUserIds(provider, userIds);
    }

    @MutationMapping
    public boolean generateAccessToken(@ContextValue LoggedInUser currentUser, @Argument GenerateAccessTokenInput input) {
        return accessTokenService.generateAccessToken(currentUser, input);
    }
}
