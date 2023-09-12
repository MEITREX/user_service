package de.unistuttgart.iste.gits.user_service.persistence.entity;

import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "CourseMembership")
@IdClass(CourseMembershipPk.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseMembershipEntity {

    @Id
    @Column(nullable = false)
    private UUID userId;

    @Id
    @Column(nullable = false)
    private UUID courseId;

    @Column(name = "courseRole")
    private UserRoleInCourse role;
}
