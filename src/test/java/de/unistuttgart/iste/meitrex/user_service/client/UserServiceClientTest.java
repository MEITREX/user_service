package de.unistuttgart.iste.meitrex.user_service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.iste.meitrex.common.testutil.GraphQlTesterParameterResolver;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.AccessToken;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalUserIdWithUser;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.exception.UserServiceConnectionException;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockAccessTokenServiceConfiguration;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@ExtendWith(GraphQlTesterParameterResolver.class)
@SpringBootTest({"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(classes = {MockKeycloakConfiguration.class, MockAccessTokenServiceConfiguration.class})
class UserServiceClientTest {

    private GraphQlClient graphQlClient;

    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        final WebTestClient webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext)
                .configureClient().baseUrl("/graphql").build();

        graphQlClient = GraphQlClient.builder(new WebTestClientTransport(webTestClient)).build();
    }

    @Test
    void testQueryUserInfos() throws UserServiceConnectionException {
        final UserServiceClient userServiceClient = new UserServiceClient(graphQlClient);

        List<UUID> userIds = List.of(MockKeycloakConfiguration.firstUserId, MockKeycloakConfiguration.secondUserId, MockKeycloakConfiguration.thirdUserId);

        List<UserInfo> queriedUserInfos = userServiceClient.queryUserInfos(userIds);

        assertThat(queriedUserInfos, hasSize(3));

        UserInfo firstUserInfo = queriedUserInfos.get(0);
        UserInfo secondUserInfo = queriedUserInfos.get(1);
        UserInfo thirdUserInfo = queriedUserInfos.get(2);

        System.out.println(firstUserInfo);
        System.out.println(secondUserInfo);
        System.out.println(thirdUserInfo);

        assertThat(firstUserInfo.getUserName(), is("firstuser"));
        assertThat(firstUserInfo.getFirstName(), is("First"));
        assertThat(firstUserInfo.getLastName(), is("User"));

        assertThat(secondUserInfo.getUserName(), is("seconduser"));
        assertThat(secondUserInfo.getFirstName(), is("Second"));
        assertThat(secondUserInfo.getLastName(), is("User"));

        assertThat(thirdUserInfo.getUserName(), is("thirduser"));
        assertThat(thirdUserInfo.getFirstName(), is("Third"));
        assertThat(thirdUserInfo.getLastName(), is("User"));

    }

    @Test
    void testQueryUserInfoNoUsers() {
        final UserServiceClient userServiceClient = new UserServiceClient(graphQlClient);

        List<UUID> userIds = List.of();

        try {
            userServiceClient.queryUserInfos(userIds);
            assertThat(true, is(false));

        } catch (UserServiceConnectionException e) {
            assertThat(e.getMessage(), is("Error fetching userInfo from UserService: UserInfo List is empty."));
        }
    }

    @Test
    void testQueryUserInfoBadId() throws UserServiceConnectionException {
        final UserServiceClient userServiceClient = new UserServiceClient(graphQlClient);

        List<UUID> userIds = List.of(MockKeycloakConfiguration.nonExistingUserId);

        List<UserInfo> queriedUserInfos = userServiceClient.queryUserInfos(userIds);

        assertThat(queriedUserInfos, hasSize(1));
        assertThat(queriedUserInfos.getFirst(), is(nullValue()));

    }

    @Test
    void testQueryUserInfoGoodAndBadId() throws UserServiceConnectionException {
        final UserServiceClient userServiceClient = new UserServiceClient(graphQlClient);

        List<UUID> userIds = List.of(MockKeycloakConfiguration.firstUserId, MockKeycloakConfiguration.nonExistingUserId, MockKeycloakConfiguration.thirdUserId);

        List<UserInfo> queriedUserInfos = userServiceClient.queryUserInfos(userIds);

        assertThat(queriedUserInfos, hasSize(3));
        assertThat(queriedUserInfos.get(0).getUserName(), is("firstuser"));
        assertThat(queriedUserInfos.get(1), is(nullValue()));
        assertThat(queriedUserInfos.get(2).getUserName(), is("thirduser"));


    }

    @Test
    void testQueryAccessToken() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();

        final LoggedInUser loggedInUser = LoggedInUser.builder()
                .id(MockKeycloakConfiguration.firstUserId)
                .userName("firstuser")
                .firstName("First")
                .lastName("User")
                .courseMemberships(List.of())
                .realmRoles(Set.of())
                .build();

        final String loggedInUserJson = objectMapper.writeValueAsString(loggedInUser);

        final WebTestClient webTestClientWithHeader = MockMvcWebTestClient
                .bindToApplicationContext(applicationContext)
                .configureClient()
                .baseUrl("/graphql")
                .defaultHeader("CurrentUser", loggedInUserJson)
                .build();

        final GraphQlClient customClient = GraphQlClient.builder(new WebTestClientTransport(webTestClientWithHeader)).build();
        final UserServiceClient userServiceClient = new UserServiceClient(customClient);

        final AccessToken accessToken = userServiceClient.queryAccessToken(loggedInUser, ExternalServiceProviderDto.GITHUB);

        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken(), is("mocked-access-token"));
        assertThat(accessToken.getExternalUserId(), is(nullValue())); // Adapt if needed
    }



    @Test
    void testQueryExternalUserIds() throws UserServiceConnectionException {
        final UserServiceClient userServiceClient = new UserServiceClient(graphQlClient);

        List<UUID> userIds = List.of(
                MockKeycloakConfiguration.firstUserId,
                MockKeycloakConfiguration.thirdUserId
        );

        List<ExternalUserIdWithUser> externalIds = userServiceClient.queryExternalUserIds(
                ExternalServiceProviderDto.GITHUB,
                userIds
        );

        assertThat(externalIds, hasSize(2));

        assertThat(externalIds.get(0).getUserId(), is(MockKeycloakConfiguration.firstUserId));
        assertThat(externalIds.get(0).getExternalUserId(), is("github_firstuser"));

        assertThat(externalIds.get(1).getUserId(), is(MockKeycloakConfiguration.thirdUserId));
        assertThat(externalIds.get(1).getExternalUserId(), is("github_thirduser"));
    }
}
