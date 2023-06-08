package de.unistuttgart.iste.gits.user_service.service;

import de.unistuttgart.iste.gits.generated.dto.CourseMembershipDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class MembershipServiceTest {

    private final CourseMembershipRepository courseMembershipRepository = Mockito.mock(CourseMembershipRepository.class);

    private final MembershipMapper membershipMapper = new MembershipMapper(new ModelMapper());

    private final MembershipService membershipService = new MembershipService(courseMembershipRepository, membershipMapper);

    @Test
    void getAllMembershipsByUserTest() {
        // init data
        List<CourseMembershipEntity> entities = new ArrayList<>();
        List<CourseMembershipDto> membershipDtos = new ArrayList<>();
        UUID userId = UUID.randomUUID();

        for (int i=0; i<3; i++){
            UUID courseId = UUID.randomUUID();
            entities.add(CourseMembershipEntity.builder().userId(userId).courseId(courseId).courseRole(CourseRole.STUDENT).build());
            membershipDtos.add(CourseMembershipDto.builder().setUserId(userId).setCourseId(courseId).setRole(CourseRole.STUDENT.toString()).build());
        }

        //mock repository
        when(courseMembershipRepository.findCourseMembershipEntitiesByUserIdOrderByCourseId(userId)).thenReturn(entities);

        // run method under test
        List<CourseMembershipDto> resultSet = membershipService.getAllMembershipsByUser(userId);


        // compare results
        assertEquals(membershipDtos.size(), resultSet.size());

        for (CourseMembershipDto item: resultSet) {
            assertTrue(membershipDtos.contains(item), item.toString());
            assertTrue(entities.contains(membershipMapper.dtoToEntity(item)), item.toString());
        }

    }
}