package indi.etern.checkIn.action.role.permission;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.role.SendPermissionsToUsersAction;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.*;

@Action("savePermissionsOfRole")
public class SavePermissionAction extends TransactionalAction {
    final RoleService roleService;
    private final ActionExecutor actionExecutor;
    Role role;
    List<String> enables;
    
    public SavePermissionAction(RoleService roleService, ActionExecutor actionExecutor) {
        this.roleService = roleService;
        this.actionExecutor = actionExecutor;
    }
    
    @Override
    public String requiredPermissionName() {
        return "edit permission";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        synchronized (roleService) {
            List<Permission> enabled = new ArrayList<>();
            for (String permissionName : enables) {
                enabled.add(roleService.findPermission(permissionName).orElseThrow());
            }
            role.getPermissions().clear();
            role.getPermissions().addAll(enabled);
            roleService.save(role);
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
            map.put("type", "success");
            actionExecutor.executeByTypeClass(SendPermissionsToUsersAction.class,role.getUsers());
            return Optional.of(map);
        }
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        role = roleService.findByType((String) dataMap.get("roleType")).orElseThrow();
        //noinspection unchecked
        enables = (List<String>) dataMap.get("enables");
    }
}
