package de.unistuttgart.iste.meitrex.user_service.persistence.mapper;

import de.unistuttgart.iste.meitrex.generated.dto.Settings;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.SettingsEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class SettingsMapper {
    private final ModelMapper modelMapper;

    public SettingsMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Settings entityToDto(SettingsEntity entity) {
        return modelMapper.map(entity, Settings.class);
    }

    public SettingsEntity dtoToEntity(Settings dto) {
        return modelMapper.map(dto, SettingsEntity.class);
    }
}
