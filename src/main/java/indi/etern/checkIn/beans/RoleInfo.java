package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleInfo {
    final RoleService roleService;
    private List<Role> roles;
    
    public RoleInfo(RoleService roleService) {
        this.roleService = roleService;
    }
    
    public List<Role> getRoles(){
        roles = roleService.findAll();
        return roles;
    }
}
