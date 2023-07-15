package de.unistuttgart.iste.gits.user_service.persistence.repository;

import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link CourseMembershipEntity}
 */
@Repository
public interface CourseMembershipRepository extends JpaRepository<CourseMembershipEntity, CourseMembershipPk> {

    /**
     * Hibernate Query. Find Entities by User ID. ORDERED BY Course ID
     * @param userId User ID
     * @return List of Entities
     */
    List<CourseMembershipEntity> findCourseMembershipEntitiesByUserIdOrderByCourseId(UUID userId);

    List<CourseMembershipEntity> findCourseMembershipEntitiesByUserIdInOrderByCourseId(List<UUID> userIds);
}
