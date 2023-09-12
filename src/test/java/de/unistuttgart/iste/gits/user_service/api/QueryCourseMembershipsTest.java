package de.unistuttgart.iste.gits.user_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import de.unistuttgart.iste.gits.user_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.user_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

@ContextConfiguration(classes = MockKeycloakConfiguration.class)
@GraphQlApiTest
class QueryCourseMembershipsTest {

    @Autowired
    private CourseMembershipRepository membershipRepository;

    @Test
    void testNoMembershipExisting(GraphQlTester tester){
        String query = """
                query courseMembership($userId: UUID!) {
                    findUserInfos(ids: [$userId]) {
                        courseMemberships {
                            userId
                            courseId
                            role
                        }
                    }
                }
                """;
        tester.document(query)
                .variable("userId", UUID.randomUUID())
                .execute()
                .path("findUserInfos[0].courseMemberships")
                .entityList(CourseMembership.class)
                .hasSize(0);
    }

    @Test
    void testMembership(GraphQlTester tester){

        UUID userId = UUID.randomUUID();
        List<CourseMembership> DTOList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            UUID courseId = UUID.randomUUID();
            CourseMembershipEntity entity = CourseMembershipEntity.builder().userId(userId).courseId(courseId).role(UserRoleInCourse.STUDENT).build();
            CourseMembership dto = CourseMembership.builder().setUserId(userId).setCourseId(courseId).setRole(UserRoleInCourse.STUDENT).build();
            membershipRepository.save(entity);
            DTOList.add(dto);
        }

        String query = """
                query courseMembership($userId: UUID!) {
                    findUserInfos(ids: [$userId]) {
                        courseMemberships {
                            userId
                            courseId
                            role
                        }
                    }
                }
                """;
        tester.document(query)
                .variable("userId", userId)
                .execute()
                .path("findUserInfos[0].courseMemberships")
                .entityList(CourseMembership.class)
                .hasSize(2)
                .contains(DTOList.get(0), DTOList.get(1));
    }

}
