package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.*;

@Action("getAllRoles")
public class GetAllRolesAction extends TransactionalAction {
    final private RoleService roleService;
    
    public GetAllRolesAction(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        LinkedHashMap<String,Object> result = getSuccessMap();
        ArrayList<Object> roleList = new ArrayList<>();
        final List<Role> roles = roleService.findAll();
        roles.sort(Comparator.comparingInt(Role::getLevel));
        roles.forEach(role -> {
            LinkedHashMap<String,Object> roleInfo = new LinkedHashMap<>();
            roleInfo.put("type", role.getType());
            roleInfo.put("level", role.getLevel());
            roleList.add(roleInfo);
        });
        result.put("roles", roleList);
        return Optional.of(result);
    }
}
