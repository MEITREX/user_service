package de.unistuttgart.iste.gits.user_service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.iste.gits.common.testutil.GraphQlTesterParameterResolver;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.generated.dto.GlobalUserRole;
import de.unistuttgart.iste.gits.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static de.unistuttgart.iste.gits.common.testutil.HeaderUtils.addCurrentUserHeader;


@ExtendWith(GraphQlTesterParameterResolver.class)
@SpringBootTest({"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(classes = MockKeycloakConfiguration.class)
class QueryUserTest {

    @Test
    void testCurrentUserInfo(HttpGraphQlTester tester) throws JsonProcessingException {
        final String username = "firstuser";
        final String firstName = "First";
        final String lastName = "User";

        LoggedInUser user = LoggedInUser.builder()
                .id(MockKeycloakConfiguration.firstUserId)
                .userName(username)
                .firstName(firstName)
                .lastName(lastName)
                .courseMemberships(Collections.emptyList())
                .realmRoles(Collections.emptySet())
                .build();


        final String query = """
                query {
                    currentUserInfo {
                        id,
                        userName,
                        firstName,
                        lastName,
                        realmRoles
                    }
                }
                """;

        tester = addCurrentUserHeader(tester, user);

        tester.document(query)
                .execute()
                .path("currentUserInfo.id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("currentUserInfo.userName").entity(String.class).isEqualTo(username)
                .path("currentUserInfo.firstName").entity(String.class).isEqualTo(firstName)
                .path("currentUserInfo.lastName").entity(String.class).isEqualTo(lastName)
                .path("currentUserInfo.realmRoles").entityList(String.class).hasSize(0);
    }

    @Test
    void testCurrentUserInfoWithRealmRole(HttpGraphQlTester tester) throws JsonProcessingException {
        final String username = "firstuser";
        final String firstName = "First";
        final String lastName = "User";

        LoggedInUser user = LoggedInUser.builder()
                .id(MockKeycloakConfiguration.firstUserId)
                .userName(username)
                .firstName(firstName)
                .lastName(lastName)
                .courseMemberships(Collections.emptyList())
                .realmRoles(Set.of(LoggedInUser.RealmRole.COURSE_CREATOR, LoggedInUser.RealmRole.SUPER_USER))
                .build();

        final String query = """
                query {
                    currentUserInfo {
                        id,
                        userName,
                        firstName,
                        lastName,
                        realmRoles
                    }
                }
                """;

        tester = addCurrentUserHeader(tester, user);

        tester.document(query)
                .execute()
                .path("currentUserInfo.id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("currentUserInfo.userName").entity(String.class).isEqualTo(username)
                .path("currentUserInfo.firstName").entity(String.class).isEqualTo(firstName)
                .path("currentUserInfo.lastName").entity(String.class).isEqualTo(lastName)
                .path("currentUserInfo.realmRoles").entityList(GlobalUserRole.class).hasSize(2).contains(GlobalUserRole.COURSE_CREATOR, GlobalUserRole.SUPER_USER);
    }

    @Test
    void testPublicUserInfoBatched(GraphQlTester tester) {
        final String query = """
                query($userIds: [UUID!]!) {
                    findPublicUserInfos(ids: $userIds) {
                        id,
                        userName
                    }
                }
                """;

        tester.document(query)
                .variable("userIds", List.of(MockKeycloakConfiguration.firstUserId,
                                                   MockKeycloakConfiguration.thirdUserId))
                .execute()
                .path("findPublicUserInfos[0].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("findPublicUserInfos[0].userName").entity(String.class).isEqualTo("firstuser")
                .path("findPublicUserInfos[1].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.thirdUserId)
                .path("findPublicUserInfos[1].userName").entity(String.class).isEqualTo("thirduser");
    }

    @Test
    void testUserInfoNonExistingUsers(GraphQlTester tester) {
        String query = """
                query($userIds: [UUID!]!) {
                    findUserInfos(ids: $userIds) {
                        id,
                        userName,
                        firstName,
                        lastName
                    }
                }
                """;

        tester.document(query)
                .variable("userIds", List.of(MockKeycloakConfiguration.nonExistingUserId))
                .execute()
                .path("findUserInfos[0]").valueIsNull();

        query = """
                query($userIds: [UUID!]!) {
                    findPublicUserInfos(ids: $userIds) {
                        id,
                        userName
                    }
                }
                """;

        tester.document(query)
                .variable("userIds", List.of(MockKeycloakConfiguration.nonExistingUserId))
                .execute()
                .path("findPublicUserInfos[0]").valueIsNull();
    }

    @Test
    void testUserInfoBatched(GraphQlTester tester) {
        final String query = """
                query($userIds: [UUID!]!) {
                    findUserInfos(ids: $userIds) {
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
                .path("findUserInfos[0].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.firstUserId)
                .path("findUserInfos[0].userName").entity(String.class).isEqualTo("firstuser")
                .path("findUserInfos[0].firstName").entity(String.class).isEqualTo("First")
                .path("findUserInfos[0].lastName").entity(String.class).isEqualTo("User")
                .path("findUserInfos[1].id").entity(UUID.class).isEqualTo(MockKeycloakConfiguration.thirdUserId)
                .path("findUserInfos[1].userName").entity(String.class).isEqualTo("thirduser")
                .path("findUserInfos[1].firstName").entity(String.class).isEqualTo("Third")
                .path("findUserInfos[1].lastName").entity(String.class).isEqualTo("User");
    }
}
