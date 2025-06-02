package de.unistuttgart.iste.meitrex.user_service.persistence.entity;

import de.unistuttgart.iste.meitrex.generated.dto.Gamification;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "GamificationSettings")
public class GamificationSettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gamification gamification;
}
