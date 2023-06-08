package de.unistuttgart.iste.gits.membership.service;

import de.unistuttgart.iste.gits.generated.dto.CourseMembershipDto;
import de.unistuttgart.iste.gits.membership.mapper.MembershipMapper;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.membership.persistence.repository.CourseMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final CourseMembershipRepository courseMembershipRepository;

    private final MembershipMapper membershipMapper;


    public List<CourseMembershipDto> getAllMembershipsByUser(UUID userId){
        //init
        List<CourseMembershipEntity> membershipEntities;

        // get entities from database
        membershipEntities = courseMembershipRepository.findCourseMembershipEntitiesByUserIdOrderByCourseId(userId);

        return membershipEntities.stream()
                .map(membershipMapper::entityToDto)
                .toList();
    }

}
