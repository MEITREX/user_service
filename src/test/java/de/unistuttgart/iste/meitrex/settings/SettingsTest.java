package de.unistuttgart.iste.meitrex.settings;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.GamificationSettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.NotificationSettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.SettingsEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.mapper.SettingsMapper;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.SettingsRepository;
import de.unistuttgart.iste.meitrex.user_service.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;


import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class SettingsTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private SettingsMapper settingsMapper;

    private SettingsService settingsService;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        settingsService = new SettingsService(settingsRepository, settingsMapper);
    }

    @Test
    void testUpdateSettingsSuccessfully() {
        // Assert
        UUID userId = UUID.randomUUID();

        // Creat existing Settings entity
        NotificationSettingsEntity notificationEntity = new NotificationSettingsEntity();
        notificationEntity.setGamification(false);
        notificationEntity.setLecture(false);

        GamificationSettingsEntity gamificationEntity = new GamificationSettingsEntity();
        gamificationEntity.setGamification(Gamification.ADAPTIVE_GAMIFICATION_ENABLED);

        SettingsEntity existingSettings = new SettingsEntity();
        existingSettings.setUserId(userId);
        existingSettings.setNotification(notificationEntity);
        existingSettings.setGamification(gamificationEntity);

        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));
        when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Create New Settings Input
        NotificationInput notificationInput = new NotificationInput();
        notificationInput.setGamification(false);
        notificationInput.setLecture(false);

        SettingsInput input = new SettingsInput();
        input.setNotification(notificationInput);
        input.setGamification(Gamification.ALL_GAMIFICATION_DISABLED);

        Settings dto = mock(Settings.class);
        when(settingsMapper.entityToDto(any())).thenReturn(dto);

        // Act
        Settings result = settingsService.update(userId, input);

        // Assert
        assertNotNull(result);
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository).save(existingSettings);
        verify(settingsMapper).entityToDto(existingSettings);

        assertFalse(existingSettings.getNotification().isGamification());
        assertFalse(existingSettings.getNotification().isLecture());
        assertEquals(Gamification.ALL_GAMIFICATION_DISABLED, existingSettings.getGamification().getGamification());
    }


    @Test
    void testSetDefaultCreatesNewSettings() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        ArgumentCaptor<SettingsEntity> captor = ArgumentCaptor.forClass(SettingsEntity.class);
        when(settingsRepository.save(any(SettingsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Settings mockDto = mock(Settings.class);
        when(settingsMapper.entityToDto(any(SettingsEntity.class))).thenReturn(mockDto);

        // Act
        Settings result = settingsService.setDefault(userId);

        // Assert
        assertNotNull(result);
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository).save(captor.capture());
        verify(settingsMapper).entityToDto(any(SettingsEntity.class));

        SettingsEntity savedEntity = captor.getValue();

        assertEquals(userId, savedEntity.getUserId());
        assertNotNull(savedEntity.getGamification());
        assertEquals(Gamification.ADAPTIVE_GAMIFICATION_ENABLED, savedEntity.getGamification().getGamification());
        assertNotNull(savedEntity.getNotification());
        assertTrue(savedEntity.getNotification().isGamification());
        assertTrue(savedEntity.getNotification().isLecture());
    }


}
