package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.repositories.PermissionGroupRepository;
import indi.etern.checkIn.repositories.PermissionRepository;
import indi.etern.checkIn.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    public static RoleService singletonInstance;
    final RoleRepository roleRepository;
    final PermissionRepository permissionRepository;
    final PermissionGroupRepository permissionGroupRepository;
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, PermissionGroupRepository permissionGroupRepository) {
        singletonInstance = this;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionGroupRepository = permissionGroupRepository;
    }
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    
    public void save(Role role) {
        roleRepository.save(role);
        final String name = "change role " + role.getType();
        if (!permissionRepository.existsByName(name)) {
            final Permission permission = new Permission(name);
            permission.setDescription("修改用户所属组组到：" + role.getType());
            PermissionGroup rolePermissionGroup = permissionGroupRepository.findById("role").orElseThrow();
            rolePermissionGroup.getPermissions().add(permission);
            permissionGroupRepository.save(rolePermissionGroup);
        }
    }
    
    public Optional<Role> findByType(String role) {
        return roleRepository.findById(role);
    }
    
    public List<PermissionGroup> getAllPermissionGroups() {
        return permissionGroupRepository.findAll();
    }
    public void savePermissionGroup(PermissionGroup permissionGroup) {
        permissionRepository.saveAll(permissionGroup.getPermissions());
        permissionGroupRepository.save(permissionGroup);
    }
    
    public Optional<Role> findById(String id) {
        return roleRepository.findById(id);
    }
    
    public Optional<Permission> findPermission(String permissionName) {
        return permissionRepository.findById(permissionName);
    }
    
    public PermissionGroup findPermissionGroupByName(String name) {
        return permissionGroupRepository.findById(name).orElseThrow();
    }
    
    public List<Permission> findAllPermission() {
        return permissionRepository.findAll();
    }
    
    public void deleteById(String roleType) {
        roleRepository.deleteById(roleType);
    }
    
    public void delete(Role role) {
        permissionRepository.deleteByName("change role "+role.getType());
        roleRepository.delete(role);
    }
}
