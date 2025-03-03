package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {
    List<Role> findAllByPermissionsContains(Permission permission);
}
