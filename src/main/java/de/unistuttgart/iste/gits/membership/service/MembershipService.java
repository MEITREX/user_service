package de.unistuttgart.iste.gits.membership.service;

import de.unistuttgart.iste.gits.generated.dto.CourseMembershipDto;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseOwnershipEntity;
import de.unistuttgart.iste.gits.membership.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.membership.persistence.repository.CourseOwnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final CourseMembershipRepository courseMembershipRepository;
    private final CourseOwnershipRepository courseOwnershipRepository;


    public CourseMembershipDto getAllMembershipsByUser(UUID userId){
        //init
        List<CourseMembershipEntity> membershipEntities;
        List<CourseOwnershipEntity> ownershipEntities;
        List<UUID> courseIds;
        List<UUID> ownedCourseIds;

        // get entities from database
        membershipEntities = courseMembershipRepository.findCourseMembershipEntitiesByUserIdOOrderByCourseId(userId);
        ownershipEntities = courseOwnershipRepository.findCourseMembershipEntitiesByUserIdOrderByCourseId(userId);

        // extract course IDs
        courseIds = membershipEntities.stream().map(CourseMembershipEntity::getCourseId).toList();
        ownedCourseIds = ownershipEntities.stream().map(CourseOwnershipEntity::getCourseId).toList();

        return CourseMembershipDto.builder()
                .setId(userId)
                .setCourses(courseIds)
                .setOwnedCourses(ownedCourseIds)
                .build();
    }

}
