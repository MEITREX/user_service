package de.unistuttgart.iste.meitrex.user_service.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenPk implements Serializable {
    private UUID userId;
    private ExternalServiceProvider provider;
}
