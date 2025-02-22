package de.unistuttgart.iste.meitrex.user_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.GenerateAccessTokenInput;
import de.unistuttgart.iste.meitrex.user_service.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AccessTokenController {

    private final AccessTokenService accessTokenService;

    @QueryMapping
    public Boolean isAccessTokenAvailable(@ContextValue LoggedInUser currentUser, @Argument ExternalServiceProviderDto provider) {
        return accessTokenService.isAccessTokenAvailable(currentUser, provider);
    }

    @QueryMapping(name="_internal_getAccessToken")
    public String getAccessToken(@ContextValue LoggedInUser currentUser, @Argument ExternalServiceProviderDto provider) {
        return accessTokenService.getAccessToken(currentUser, provider);
    }

    @MutationMapping
    public Boolean generateAccessToken(@ContextValue LoggedInUser currentUser, @Argument GenerateAccessTokenInput input) {
        return accessTokenService.generateAccessToken(currentUser, input);
    }
}
