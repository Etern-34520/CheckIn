package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.service.dao.RoleService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionInfo {
    private final RoleService roleService;
    private List<PermissionGroup> permissionGroups;
    
    public PermissionInfo(RoleService roleService) {
        this.roleService = roleService;
    }
    
    public List<PermissionGroup> getPermissionGroups() {
        permissionGroups = roleService.getAllPermissionGroups();
        return permissionGroups;
    }
}
