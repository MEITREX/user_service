package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.GamificationSettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.NotificationSettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.SettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.mapper.SettingsMapper;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {
    private final SettingsRepository settingsRepository;
    private final SettingsMapper settingsMapper;

    public Settings save(UUID userId, SettingsInput input) {
        Optional<SettingsEntity> optional = settingsRepository.findByUserId(userId);
        SettingsEntity settings = optional.orElseGet(SettingsEntity::new);
        settings.setUserId(userId);

        // Set Notification settings
        NotificationSettingsEntity notification = Optional.ofNullable(settings.getNotification())
                .orElseGet(NotificationSettingsEntity::new);
        notification.setGamification(input.getNotification().getGamification());
        notification.setLecture(input.getNotification().getLecture());
        settings.setNotification(notification);

        // Set Gamification Settings
        GamificationSettingsEntity gamification = Optional.ofNullable(settings.getGamification())
                        .orElseGet(GamificationSettingsEntity::new);
        gamification.setGamification(input.getGamification());
        settings.setGamification(gamification);

        SettingsEntity saved = settingsRepository.save(settings);
        return settingsMapper.entityToDto(saved);
    }
}
