package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action(name = "createRole")
public class CreateRoleAction extends TransactionalAction {
    Role role;
    List<String> enabledPermissionIdList;
    
    @Override
    public String requiredPermissionName() {
        return "create role";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final List<Permission> permissions = RoleService.singletonInstance.findAllPermission();
        for (Permission permission : permissions) {
            if (enabledPermissionIdList.contains(permission.getId()))
                role.getPermissions().add(permission);
        }
        RoleService.singletonInstance.save(role);
        return successOptionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        role = new Role((String) dataMap.get("roleName"));
        //noinspection unchecked
        enabledPermissionIdList = (List<String>) dataMap.get("enable");
    }
}
