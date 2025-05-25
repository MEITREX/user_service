package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.GamificationSettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.NotificationSettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.SettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.mapper.SettingsMapper;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.SettingsRepository;
import jakarta.persistence.EntityNotFoundException;
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

     /**
     * Update user settings
     * @param input settings to be updated
     * @param userId
     * @return updated user settings
     */
    public Settings update(UUID userId, SettingsInput input) {
        SettingsEntity settings = settingsRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No settings found for user "));

        // Update Notification Settings if present
        if (settings.getNotification() != null && input.getNotification() != null) {
            NotificationSettingsEntity notification = settings.getNotification();
            notification.setGamification(input.getNotification().getGamification());
            notification.setLecture(input.getNotification().getLecture());
        }

        // Update Gamification Settings if present
        if (settings.getGamification() != null && input.getGamification() != null) {
            GamificationSettingsEntity gamification = settings.getGamification();
            gamification.setGamification(input.getGamification());
        }

        // Save and return DTO
        SettingsEntity saved = settingsRepository.save(settings);
        return settingsMapper.entityToDto(saved);
    }

     /**
     * Return settings of user
     * @param userId
     * @return user settings
     */
    public Settings find(UUID userId) {
        Optional<SettingsEntity> optional = settingsRepository.findByUserId(userId);
        return optional.map(settingsMapper::entityToDto).orElse(null);
    }

     /**
     * Set user settings to default to default settings
     * @param userId
     * @return default user settings
     */
    public Settings setDefault(UUID userId) {
        // Get or Create User Settings
        SettingsEntity settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> SettingsEntity.builder().userId(userId).build());

        // Set Default Gamification Settings
        GamificationSettingsEntity gamificationSettings = GamificationSettingsEntity.builder()
                .gamification(Gamification.ADAPTIVE_GAMIFICATION_ENABLED)
                .build();

        // Set Default Notification Settings
        NotificationSettingsEntity notificationSettings = NotificationSettingsEntity.builder()
                .gamification(true)
                .lecture(true)
                .build();

        settings.setGamification(gamificationSettings);
        settings.setNotification(notificationSettings);

        SettingsEntity saved = settingsRepository.save(settings);
        return settingsMapper.entityToDto(saved);
    }
}
