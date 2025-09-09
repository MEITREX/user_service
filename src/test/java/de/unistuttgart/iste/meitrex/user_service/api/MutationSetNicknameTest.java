package de.unistuttgart.iste.meitrex.user_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockAccessTokenServiceConfiguration;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.HeaderUtils.addCurrentUserHeader;
import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@GraphQlApiTest
@ContextConfiguration(classes = {
        MockKeycloakConfiguration.class,
        MockAccessTokenServiceConfiguration.class
})
public class MutationSetNicknameTest {

    UUID courseId = UUID.randomUUID();

    private final LoggedInUser user = LoggedInUser.builder()
            .id(MockKeycloakConfiguration.firstUserId)
            .userName("firstuser")
            .firstName("First")
            .lastName("User")
            .courseMemberships(Collections.emptyList())
            .realmRoles(Set.of())
            .build();

    @Test
    void setNickname(WebGraphQlTester tester) {
        final String query = """
                mutation {
                    setNickname(nickname: "TestNickname"){
                        id
                        nickname
                        firstName
                    }
                }
                """;
        tester = addCurrentUserHeader(tester, user);

        UserInfo userInfo = tester.document(query)
                .execute()
                .path("setNickname")
                .entity(UserInfo.class).get();
        assertThat(userInfo.getNickname(), is("TestNickname"));
    }
}
