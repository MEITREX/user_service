package de.unistuttgart.iste.gits.user_service.service;

import de.unistuttgart.iste.gits.common.event.CourseChangeEvent;
import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.user_service.mapper.MembershipMapper;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseRole;
import de.unistuttgart.iste.gits.user_service.persistence.repository.CourseMembershipRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MembershipServiceTest {

    private final CourseMembershipRepository courseMembershipRepository = Mockito.mock(CourseMembershipRepository.class);

    private final MembershipMapper membershipMapper = new MembershipMapper(new ModelMapper());

    private final MembershipService membershipService = new MembershipService(courseMembershipRepository, membershipMapper);

    @Test
    void getAllMembershipsByUserTest() {
        // init data
        List<CourseMembershipEntity> entities = new ArrayList<>();
        List<CourseMembership> membershipDtos = new ArrayList<>();
        UUID userId = UUID.randomUUID();

        for (int i=0; i<3; i++){
            UUID courseId = UUID.randomUUID();
            entities.add(CourseMembershipEntity.builder().userId(userId).courseId(courseId).courseRole(CourseRole.STUDENT).build());
            membershipDtos.add(CourseMembership.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build());
        }

        //mock repository
        when(courseMembershipRepository.findCourseMembershipEntitiesByUserIdOrderByCourseId(userId)).thenReturn(entities);

        // run method under test
        List<CourseMembership> resultSet = membershipService.getAllMembershipsByUser(userId);


        // compare results
        assertEquals(membershipDtos.size(), resultSet.size());

        for (CourseMembership item: resultSet) {
            assertTrue(membershipDtos.contains(item), item.toString());
        }

    }

    @Test
    void removeMembershipEventTest(){
        UUID courseId = UUID.randomUUID();
        CourseChangeEvent courseDeletionEvent = CourseChangeEvent.builder().courseId(courseId).operation(CrudOperation.DELETE).build();

        List<CourseMembershipEntity> memberships = List.of(CourseMembershipEntity.builder()
                        .courseId(courseId)
                        .userId(UUID.randomUUID())
                        .courseRole(CourseRole.STUDENT)
                        .build(),
                CourseMembershipEntity.builder()
                        .courseId(courseId)
                        .userId(UUID.randomUUID())
                        .courseRole(CourseRole.ADMINISTRATOR)
                        .build()
                );

        //mock repository
        when(courseMembershipRepository.findCourseMembershipEntitiesByCourseId(courseId)).thenReturn(memberships);

        //execute method under test
        assertDoesNotThrow( () -> membershipService.removeCourse(courseDeletionEvent));

        //verify called methods
        verify(courseMembershipRepository).deleteAll(memberships);

    }

    @Test
    void noRemoveMembershipEventTest(){
        CourseChangeEvent courseCreateEvent = CourseChangeEvent.builder().courseId(UUID.randomUUID()).operation(CrudOperation.CREATE).build();
        CourseChangeEvent courseUpdateEvent = CourseChangeEvent.builder().courseId(UUID.randomUUID()).operation(CrudOperation.UPDATE).build();

        //execute method under test
        assertDoesNotThrow( () -> membershipService.removeCourse(courseCreateEvent));
        assertDoesNotThrow( () -> membershipService.removeCourse(courseUpdateEvent));

        //verify called methods
        verify(courseMembershipRepository, never()).deleteAll(any());
    }
    @Test
    void incompleteCourseEventTest(){
        CourseChangeEvent courseEventNoId = CourseChangeEvent.builder().operation(CrudOperation.CREATE).build();
        CourseChangeEvent courseEventNoOperation = CourseChangeEvent.builder().courseId(UUID.randomUUID()).build();

        //execute method under test
        assertThrows(NullPointerException.class, () -> membershipService.removeCourse(courseEventNoId));
        assertThrows(NullPointerException.class, () -> membershipService.removeCourse(courseEventNoOperation));
    }
}