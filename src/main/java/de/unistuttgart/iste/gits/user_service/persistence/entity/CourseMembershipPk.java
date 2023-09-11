package de.unistuttgart.iste.gits.user_service.persistence.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseMembershipPk implements Serializable {

    private UUID userId;

    private UUID courseId;
}
