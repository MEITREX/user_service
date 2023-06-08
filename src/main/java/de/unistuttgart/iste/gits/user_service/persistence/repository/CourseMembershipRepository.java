package de.unistuttgart.iste.gits.user_service.persistence.repository;

import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.user_service.persistence.dao.CourseMembershipPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseMembershipRepository extends JpaRepository<CourseMembershipEntity, CourseMembershipPk> {

    List<CourseMembershipEntity> findCourseMembershipEntitiesByUserIdOrderByCourseId(UUID userId);

}
