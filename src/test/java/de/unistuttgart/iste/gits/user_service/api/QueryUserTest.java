package de.unistuttgart.iste.gits.user_service.api;

import de.unistuttgart.iste.gits.common.testutil.ClearDatabase;
import de.unistuttgart.iste.gits.common.testutil.GitsPostgresSqlContainer;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.GraphQlTesterParameterResolver;
import de.unistuttgart.iste.gits.user_service.config.KeycloakWrapper;
import de.unistuttgart.iste.gits.user_service.service.UserService;
import de.unistuttgart.iste.gits.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.*;
import java.util.List;
import java.util.UUID;

@ContextConfiguration(classes = MockKeycloakConfiguration.class)
@GraphQlApiTest
class QueryUserTest {

    @Test
    void testPublicUserInfo(GraphQlTester tester) {
        String query = """
                query($userId: UUID!) {
                    publicUserInfo(id: $userId) {
                        id,
                        userName
                    }
                }
                """;

        tester.document(query)
                .variable("userId", MockKeycloakConfiguration.firstUserId)
                .execute()
                .path("publicUserInfo.id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("publicUserInfo.userName").entity(String.class).isEqualTo("firstuser");
    }

    @Test
    void testUserInfo(GraphQlTester tester) {
        String query = """
                query($userId: UUID!) {
                    userInfo(id: $userId) {
                        id,
                        userName,
                        firstName,
                        lastName
                    }
                }
                """;

        tester.document(query)
                .variable("userId", MockKeycloakConfiguration.firstUserId)
                .execute()
                .path("userInfo.id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("userInfo.userName").entity(String.class).isEqualTo("firstuser")
                .path("userInfo.firstName").entity(String.class).isEqualTo("First")
                .path("userInfo.lastName").entity(String.class).isEqualTo("User");
    }

    @Test
    void testPublicUserInfoBatched(GraphQlTester tester) {
        String query = """
                query($userIds: [UUID!]!) {
                    publicUserInfoBatched(ids: $userIds) {
                        id,
                        userName
                    }
                }
                """;

        tester.document(query)
                .variable("userIds", List.of(MockKeycloakConfiguration.firstUserId,
                                                   MockKeycloakConfiguration.thirdUserId))
                .execute()
                .path("publicUserInfoBatched[0].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("publicUserInfoBatched[0].userName").entity(String.class).isEqualTo("firstuser")
                .path("publicUserInfoBatched[1].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.thirdUserId)
                .path("publicUserInfoBatched[1].userName").entity(String.class).isEqualTo("thirduser");
    }

    @Test
    void testUserInfoBatched(GraphQlTester tester) {
        String query = """
                query($userIds: [UUID!]!) {
                    userInfoBatched(ids: $userIds) {
                        id,
                        userName,
                        firstName,
                        lastName
                    }
                }
                """;

        tester.document(query)
                .variable("userIds", List.of(MockKeycloakConfiguration.firstUserId,
                                                   MockKeycloakConfiguration.thirdUserId))
                .execute()
                .path("userInfoBatched[0].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("userInfoBatched[0].userName").entity(String.class).isEqualTo("firstuser")
                .path("userInfoBatched[0].firstName").entity(String.class).isEqualTo("First")
                .path("userInfoBatched[0].lastName").entity(String.class).isEqualTo("User")
                .path("userInfoBatched[1].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.thirdUserId)
                .path("userInfoBatched[1].userName").entity(String.class).isEqualTo("thirduser")
                .path("userInfoBatched[1].firstName").entity(String.class).isEqualTo("Third")
                .path("userInfoBatched[1].lastName").entity(String.class).isEqualTo("User");
    }
}
