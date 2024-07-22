package indi.etern.checkIn.service.dao;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.repositories.PermissionGroupRepository;
import indi.etern.checkIn.repositories.PermissionRepository;
import indi.etern.checkIn.repositories.RoleRepository;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.stereotype.Service;

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
    
    public void save(Role role) {
        roleRepository.save(role);
        final String name = "change role " + role.getType();
        if (!permissionRepository.existsByName(name)) {
            final Permission permission = new Permission(name);
            permission.setDescription("修改用户所属组组到：" + role.getType());
            PermissionGroup rolePermissionGroup = permissionGroupRepository.findById("role").orElseThrow();
            rolePermissionGroup.getPermissions().add(permission);
            permissionGroupRepository.save(rolePermissionGroup);
            
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "addPermission");
            JsonObject permissionJson = new JsonObject();
            permissionJson.addProperty("name", permission.getName());
            permissionJson.addProperty("description", permission.getDescription());
            permissionJson.addProperty("group", "role");
            jsonObject.add("permission", permissionJson);
            
            webSocketService.sendMessageToAll(jsonObject);
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
    
    public void delete(Role role) {
        final String permissionName = "change role " + role.getType();
        permissionRepository.deleteByName(permissionName);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "deletePermission");
        JsonObject permissionJson = new JsonObject();
        permissionJson.addProperty("name", permissionName);
        permissionJson.addProperty("group", "role");
        jsonObject.add("permission", permissionJson);
        
        webSocketService.sendMessageToAll(jsonObject);
        
        roleRepository.delete(role);
    }
    
    public int count() {
        return (int) roleRepository.count();
    }
    
    public void saveAll(List<Role> roles) {
        roleRepository.saveAll(roles);
    }
}
