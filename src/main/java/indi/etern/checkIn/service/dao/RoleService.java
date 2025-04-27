package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.repositories.PermissionGroupRepository;
import indi.etern.checkIn.repositories.PermissionRepository;
import indi.etern.checkIn.repositories.RoleRepository;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    public static RoleService singletonInstance;
    final RoleRepository roleRepository;
    final PermissionRepository permissionRepository;
    final PermissionGroupRepository permissionGroupRepository;
    private final WebSocketService webSocketService;
    
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, PermissionGroupRepository permissionGroupRepository, WebSocketService webSocketService) {
        singletonInstance = this;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionGroupRepository = permissionGroupRepository;
        this.webSocketService = webSocketService;
    }
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    
    public Permission save(Role role) {
        final String name = "change role to " + role.getType();
        if (!permissionRepository.existsByName(name)) {
            final Permission permission = new Permission(name);
            permission.setDescription("修改用户所属组组到 " + role.getType());
            PermissionGroup rolePermissionGroup = permissionGroupRepository.findById("role").orElseThrow();
            permission.setGroup(rolePermissionGroup);
            permissionRepository.save(permission);
            roleRepository.save(role);
            
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
            map.put("type", "addPermission");
            LinkedHashMap<String,Object> permissionJson = new LinkedHashMap<>();
            permissionJson.put("name", permission.getName());
            permissionJson.put("description", permission.getDescription());
            permissionJson.put("group", "role");
            map.put("permission", permissionJson);
            
            webSocketService.sendMessageToAll(map);
            return permission;
        } else {
            roleRepository.save(role);
            return null;
        }
    }
    
    public Optional<Role> findByType(String role) {
        return roleRepository.findById(role);
    }
    
    public List<PermissionGroup> getAllPermissionGroups() {
        return permissionGroupRepository.findAll();
    }
    public void savePermission(Permission permission) {
        permissionRepository.save(permission);
    }
    
    public Optional<Role> findById(String id) {
        return roleRepository.findById(id);
    }
    
    public Optional<Permission> findPermission(String permissionName) {
        return permissionRepository.findByName(permissionName);
    }
    
    public PermissionGroup findPermissionGroupByName(String name) {
        return permissionGroupRepository.findById(name).orElseThrow();
    }
    public List<PermissionGroup> findAllPermissionGroup() {
        return permissionGroupRepository.findAll();
    }
    
    public List<Permission> findAllPermission() {
        return permissionRepository.findAll();
    }
    
    public void deleteById(String roleType) {
        roleRepository.deleteById(roleType);
    }
    
    @Transactional
    public void delete(Role role) {
        final String permissionName = "change role to " + role.getType();
        final Optional<Permission> byName = permissionRepository.findByName(permissionName);
        if (byName.isPresent()) {
            final Permission permission = byName.get();
            final List<Role> rolesHasThisPermission = List.copyOf(roleRepository.findAllByPermissionsContains(permission));
            for (Role role1 : rolesHasThisPermission) {
                role1.getPermissions().remove(permission);
                roleRepository.save(role1);
            }
            permissionRepository.delete(permission);
        }
        
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", "deletePermission");
        LinkedHashMap<String,Object> permissionJson = new LinkedHashMap<>();
        permissionJson.put("name", permissionName);
        permissionJson.put("group", "role");
        map.put("permission", permissionJson);
        
        webSocketService.sendMessageToAll(map);
        
        roleRepository.delete(role);
    }
    
    public int count() {
        return (int) roleRepository.count();
    }
    
    public void saveAll(List<Role> roles) {
        roleRepository.saveAll(roles);
    }
    
    public boolean existByType(String type) {
        return roleRepository.existsById(type);
    }
}
