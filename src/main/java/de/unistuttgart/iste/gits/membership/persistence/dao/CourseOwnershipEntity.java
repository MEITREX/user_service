package de.unistuttgart.iste.gits.membership.persistence.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "CourseOwnership")
@IdClass(CourseMembershipPk.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOwnershipEntity {

    @Id
    @Column(nullable = false)
    private UUID userId;

    @Id
    @Column(nullable = false)
    private UUID courseId;
}

