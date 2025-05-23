package de.unistuttgart.iste.meitrex.user_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Settings")
public class SettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID userId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "notification_id", referencedColumnName = "id", nullable = false)
    private NotificationSettingsEntity notification;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "gamification_id", referencedColumnName = "id", nullable = false)
    private GamificationSettingsEntity gamification;
}
