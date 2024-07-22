package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    void deleteByName(String name);
    
    boolean existsByName(String name);
    
    Optional<Permission> findByName(String permissionName);
}
