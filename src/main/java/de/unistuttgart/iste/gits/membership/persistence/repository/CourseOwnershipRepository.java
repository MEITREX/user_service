package de.unistuttgart.iste.gits.membership.persistence.repository;

import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipPk;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseOwnershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseOwnershipRepository extends JpaRepository<CourseMembershipEntity, CourseMembershipPk> {

    List<CourseOwnershipEntity> findCourseMembershipEntitiesByUserIdOrderByCourseId(UUID userId);
}
