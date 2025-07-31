package de.unistuttgart.iste.meitrex.user_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockAccessTokenServiceConfiguration;
import de.unistuttgart.iste.meitrex.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.HeaderUtils.addCurrentUserHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@GraphQlApiTest
@ContextConfiguration(classes = {
        MockKeycloakConfiguration.class,
        MockAccessTokenServiceConfiguration.class
})
public class QueryUserNicknameByUserId {

    UUID courseId = UUID.randomUUID();

    private final LoggedInUser user = LoggedInUser.builder()
            .id(MockKeycloakConfiguration.firstUserId)
            .userName("firstuser")
            .firstName("First")
            .lastName("User")
            .courseMemberships(Collections.emptyList())
            .realmRoles(Set.of())
            .build();
    @Autowired
    private UserRepository userRepository;

    @Test
    void testQueryUserNicknameByUserId(WebGraphQlTester tester) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(MockKeycloakConfiguration.firstUserId);
        userEntity.setNickname("TestNickname");
        userRepository.save(userEntity);
        final String query = """
                query {
                    _internal_noauth_userNicknameByUserId(userId: "%s"){
                        id
                        nickname
                        firstName
                    }
                }
                """.formatted(MockKeycloakConfiguration.firstUserId);

        tester = addCurrentUserHeader(tester, user);

        UserInfo userInfo = tester.document(query)
                .execute()
                .path("_internal_noauth_userNicknameByUserId")
                .entity(UserInfo.class).get();
        assertThat(userInfo.getNickname(), is("TestNickname"));
    }
}
