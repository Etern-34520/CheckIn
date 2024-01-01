package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.List;
import java.util.Optional;

public class CreateRoleAction extends JsonResultAction {
    Role role;
    List<String> enabledPermissionIdList;
    
    public CreateRoleAction(String roleName, List<String> enabledPermissionIdList) {
        role = new Role(roleName);
        this.enabledPermissionIdList = enabledPermissionIdList;
    }
    
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "success");
        return Optional.of(jsonObject);
    }
}
