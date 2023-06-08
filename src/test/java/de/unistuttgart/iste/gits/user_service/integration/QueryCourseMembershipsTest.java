package de.unistuttgart.iste.gits.user_service.integration;

import de.unistuttgart.iste.gits.generated.dto.CourseMembershipDto;
import de.unistuttgart.iste.gits.user_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.util.GraphQlApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

@GraphQlApiTest
public class QueryCourseMembershipsTest {

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
                .entityList(CourseMembershipDto.class)
                .hasSize(0);
    }
}
