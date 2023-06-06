package de.unistuttgart.iste.gits.membership.persistence.repository;

import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipEntity;
import de.unistuttgart.iste.gits.membership.persistence.dao.CourseMembershipPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseMembershipRepository extends JpaRepository<CourseMembershipEntity, CourseMembershipPk> {

    List<CourseMembershipEntity> findCourseMembershipEntitiesByUserIdOOrderByCourseId(UUID userId);

}
