package de.unistuttgart.iste.gits.membership.mapper;

import de.unistuttgart.iste.gits.generated.dto.CourseMembershipDto;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipMapper {

    private final ModelMapper modelMapper;


    public CourseMembershipDto entityToDto(CourseMembershipEntity courseMembershipEntity){

        return modelMapper.map(courseMembershipEntity, CourseMembershipDto.class);

    }

    public CourseMembershipEntity dtoToEntity(CourseMembershipDto courseMembershipDto){

        return modelMapper.map(courseMembershipDto, CourseMembershipEntity.class);
    }
}
