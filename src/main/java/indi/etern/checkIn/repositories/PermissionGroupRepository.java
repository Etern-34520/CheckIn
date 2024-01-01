package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.user.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, String> {
}
