package de.unistuttgart.iste.gits.user_service.api;

import de.unistuttgart.iste.gits.common.testutil.GitsPostgresSqlContainer;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseRole;
import de.unistuttgart.iste.gits.user_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.user_service.test_config.MockKeycloakConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ContextConfiguration(classes = MockKeycloakConfiguration.class)
@GraphQlApiTest
public class QueryCourseMembershipsTest {

    @Container
    public static PostgreSQLContainer<GitsPostgresSqlContainer> postgreSQLContainer = GitsPostgresSqlContainer.getInstance();

    @Autowired
    private CourseMembershipRepository membershipRepository;

    @Test
    void testNoMembershipExisting(GraphQlTester tester){
        //GraphQL query
        String query = """
                query {
                    courseMemberships(id: "%s") {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(UUID.randomUUID());
        tester.document(query)
                .execute()
                .path("courseMemberships")
                .entityList(CourseMembership.class)
                .hasSize(0);
    }

    @Test
    void testMembership(GraphQlTester tester){

        UUID userId = UUID.randomUUID();
        List<CourseMembership> DTOList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            UUID courseId = UUID.randomUUID();
            CourseMembershipEntity entity = CourseMembershipEntity.builder().userId(userId).courseId(courseId).courseRole(CourseRole.STUDENT).build();
            CourseMembership dto = CourseMembership.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build();
            membershipRepository.save(entity);
            DTOList.add(dto);
        }
        //GraphQL query
        String query = """
                query {
                    courseMemberships(id: "%s") {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(userId);
        tester.document(query)
                .execute()
                .path("courseMemberships")
                .entityList(CourseMembership.class)
                .hasSize(2)
                .contains(DTOList.get(0), DTOList.get(1));
    }

    @Test
    void testMembershipBatched(GraphQlTester tester) {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        UUID course1Id = UUID.randomUUID();
        UUID course2Id = UUID.randomUUID();

        CourseMembershipEntity membership1 = CourseMembershipEntity.builder()
                .userId(user1Id)
                .courseId(course1Id)
                .courseRole(CourseRole.STUDENT)
                .build();
        membershipRepository.save(membership1);

        CourseMembershipEntity membership2 = CourseMembershipEntity.builder()
                .userId(user2Id)
                .courseId(course2Id)
                .courseRole(CourseRole.ADMINISTRATOR)
                .build();
        membershipRepository.save(membership2);

        String query = """
                query($userIds: [UUID!]!) {
                    courseMembershipsBatched(ids: $userIds) {
                        userId
                        courseId
                        role
                    }
                }
                """;

        tester.document(query)
                .variable("userIds", List.of(user1Id, user2Id))
                .execute()
                .path("courseMembershipsBatched[0][0].userId").entity(UUID.class).isEqualTo(user1Id)
                .path("courseMembershipsBatched[0][0].courseId").entity(UUID.class).isEqualTo(course1Id)
                .path("courseMembershipsBatched[0][0].role").entity(String.class).isEqualTo(CourseRole.STUDENT.toString())
                .path("courseMembershipsBatched[1][0].userId").entity(UUID.class).isEqualTo(user2Id)
                .path("courseMembershipsBatched[1][0].courseId").entity(UUID.class).isEqualTo(course2Id)
                .path("courseMembershipsBatched[1][0].role").entity(String.class).isEqualTo(CourseRole.ADMINISTRATOR.toString());
    }
}
