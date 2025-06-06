package de.unistuttgart.iste.meitrex.user_service.persistence.repository;

import de.unistuttgart.iste.meitrex.user_service.persistence.entity.SettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface SettingsRepository extends JpaRepository<SettingsEntity, Long> {
    Optional<SettingsEntity> findByUserId(UUID userId);
    List<SettingsEntity> findAllByUserIdIn(List<UUID> userIds);
}
