package de.unistuttgart.iste.meitrex.user_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "NotificationSettings")
public class NotificationSettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private boolean gamification;

    @Column(nullable = false)
    private boolean lecture;
}
