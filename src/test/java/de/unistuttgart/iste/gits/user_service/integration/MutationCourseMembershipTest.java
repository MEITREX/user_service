package de.unistuttgart.iste.gits.user_service.integration;

import de.unistuttgart.iste.gits.generated.dto.CourseMembershipDto;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseRole;
import de.unistuttgart.iste.gits.user_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.util.GraphQlApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

@GraphQlApiTest
public class MutationCourseMembershipTest {

    @Autowired
    CourseMembershipRepository courseMembershipRepository;

    @Test
    void createMembershipTest(GraphQlTester tester){

        CourseMembershipDto expectedDto = CourseMembershipDto.builder().setUserId(UUID.randomUUID()).setCourseId(UUID.randomUUID()).setRole(CourseRole.STUDENT.toString()).build();

        String query = """
                mutation {
                    createMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: "%s"
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("createMembership")
                .entity(CourseMembershipDto.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void updateMembershipMembershipNotExistingTest(GraphQlTester tester){

        //init input data
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        CourseMembershipDto expectedDto = CourseMembershipDto.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build();

        String query = """
                mutation {
                    updateMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: "%s"
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().contains("not member in course"));
    }

    @Test
    void updateMembershipTest(GraphQlTester tester){

        //init input data
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        CourseMembershipEntity entity = CourseMembershipEntity.builder().userId(userId).courseId(courseId).courseRole(CourseRole.STUDENT).build();
        CourseMembershipDto expectedDto = CourseMembershipDto.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build();

        //create entity in DB
        courseMembershipRepository.save(entity);

        String query = """
                mutation {
                    updateMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: "%s"
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("updateMembership")
                .entity(CourseMembershipDto.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void deleteNotExistingMembershipTest(GraphQlTester tester){

        //init input data
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        CourseMembershipDto expectedDto = CourseMembershipDto.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build();


        String query = """
                mutation {
                    deleteMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: "%s"
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().contains("not member in course"));
    }

    @Test
    void deleteMembershipTest(GraphQlTester tester){

        //init input data
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        CourseMembershipEntity entity = CourseMembershipEntity.builder().userId(userId).courseId(courseId).courseRole(CourseRole.STUDENT).build();
        CourseMembershipDto expectedDto = CourseMembershipDto.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build();

        //create entity in DB
        courseMembershipRepository.save(entity);

        String query = """
                mutation {
                    deleteMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: "%s"
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("deleteMembership")
                .entity(CourseMembershipDto.class)
                .isEqualTo(expectedDto);
    }

}
