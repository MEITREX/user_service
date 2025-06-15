package de.unistuttgart.iste.meitrex.user_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.AccessToken;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalUserIdWithUser;
import de.unistuttgart.iste.meitrex.generated.dto.GenerateAccessTokenInput;
import de.unistuttgart.iste.meitrex.user_service.service.AccessTokenService;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockKeycloakConfiguration;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockAccessTokenServiceConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.List;

import static de.unistuttgart.iste.meitrex.common.testutil.HeaderUtils.addCurrentUserHeader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@GraphQlApiTest
@ContextConfiguration(classes = {
        MockKeycloakConfiguration.class,
        MockAccessTokenServiceConfiguration.class
})
class QueryAccessTokenTest {

    @Autowired
    private AccessTokenService accessTokenService;

    private final LoggedInUser user = LoggedInUser.builder()
            .id(MockKeycloakConfiguration.firstUserId)
            .userName("firstuser")
            .firstName("First")
            .lastName("User")
            .courseMemberships(Collections.emptyList())
            .realmRoles(Set.of())
            .build();

    @Test
    void testIsAccessTokenAvailable(WebGraphQlTester tester) {
        when(accessTokenService.isAccessTokenAvailable(any(), eq(ExternalServiceProviderDto.GITHUB)))
                .thenReturn(true);

        String query = """
            query($provider: ExternalServiceProviderDto!) {
                isAccessTokenAvailable(provider: $provider)
            }
        """;

        tester = addCurrentUserHeader(tester, user);

        tester.document(query)
                .variable("provider", ExternalServiceProviderDto.GITHUB)
                .execute()
                .path("isAccessTokenAvailable").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void testInternalGetAccessToken(WebGraphQlTester tester) {
        String token = "mocked-access-token";
        when(accessTokenService.getAccessToken(any(), eq(ExternalServiceProviderDto.GITHUB)))
                .thenReturn(new AccessToken(token, null));

        String query = """
            query($currentUserId: UUID!, $provider: ExternalServiceProviderDto!) {
                _internal_getAccessToken(currentUserId: $currentUserId, provider: $provider) {
                    accessToken
                }
            }
        """;

        tester.document(query)
                .variable("currentUserId", user.getId())
                .variable("provider", ExternalServiceProviderDto.GITHUB)
                .execute()
                .path("_internal_getAccessToken.accessToken")
                .entity(String.class)
                .isEqualTo(token);
    }

    @Test
    void testGenerateAccessToken(WebGraphQlTester tester) {
        when(accessTokenService.generateAccessToken(any(), any()))
                .thenReturn(true);

        String mutation = """
            mutation($input: GenerateAccessTokenInput!) {
                generateAccessToken(input: $input)
            }
        """;

        GenerateAccessTokenInput input = new GenerateAccessTokenInput();
        input.setProvider(ExternalServiceProviderDto.GITHUB);
        input.setAuthorizationCode("mockCode");

        tester = addCurrentUserHeader(tester, user);

        tester.document(mutation)
                .variable("input", input)
                .execute()
                .path("generateAccessToken").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void testInternalGetExternalUserIds(WebGraphQlTester tester) {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        var expectedResult = List.of(
                new ExternalUserIdWithUser(userId1, "user1"),
                new ExternalUserIdWithUser(userId2, "user2")
        );

        when(accessTokenService.getExternalUserIds(
                eq(ExternalServiceProviderDto.GITHUB),
                eq(List.of(userId1, userId2)))
        ).thenReturn(expectedResult);

        String query = """
        query($provider: ExternalServiceProviderDto!, $userIds: [UUID!]!) {
            _internal_getExternalUserIds(provider: $provider, userIds: $userIds) {
                userId
                externalUserId
            }
        }
    """;

        List<ExternalUserIdWithUser> result = tester.document(query)
                .variable("provider", ExternalServiceProviderDto.GITHUB)
                .variable("userIds", List.of(userId1, userId2))
                .execute()
                .path("_internal_getExternalUserIds")
                .entityList(ExternalUserIdWithUser.class)
                .get();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedResult));
    }

    @Test
    void testInternalGetExternalUserIds_emptyList(WebGraphQlTester tester) {
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(accessTokenService.getExternalUserIds(
                eq(ExternalServiceProviderDto.GITHUB),
                eq(userIds))
        ).thenReturn(List.of());

        String query = """
        query($provider: ExternalServiceProviderDto!, $userIds: [UUID!]!) {
            _internal_getExternalUserIds(provider: $provider, userIds: $userIds) {
                userId
                externalUserId
            }
        }
    """;

        tester.document(query)
                .variable("provider", ExternalServiceProviderDto.GITHUB)
                .variable("userIds", userIds)
                .execute()
                .path("_internal_getExternalUserIds")
                .entityList(ExternalUserIdWithUser.class)
                .hasSize(0);
    }

}
