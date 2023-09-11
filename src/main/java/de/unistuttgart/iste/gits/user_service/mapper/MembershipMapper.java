package de.unistuttgart.iste.gits.user_service.mapper;

import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.CourseMembershipInput;
import de.unistuttgart.iste.gits.user_service.persistence.entity.CourseMembershipEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipMapper {

    private final ModelMapper modelMapper;


    public CourseMembership entityToDto(CourseMembershipEntity courseMembershipEntity){

        return modelMapper.map(courseMembershipEntity, CourseMembership.class);

    }

    public CourseMembershipEntity dtoToEntity(CourseMembershipInput membershipInput){

        return modelMapper.map(membershipInput, CourseMembershipEntity.class);
    }
}
