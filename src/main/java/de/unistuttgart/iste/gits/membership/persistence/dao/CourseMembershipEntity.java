package de.unistuttgart.iste.gits.membership.persistence.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
