package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.Optional;

public class DeleteRoleAction extends TransactionalAction {
    String roleType;
    public DeleteRoleAction(String roleType) {
        this.roleType = roleType;
    }
    @Override
    public String requiredPermissionName() {
        return "delete role";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Role role = RoleService.singletonInstance.findByType(roleType).orElseThrow();
        if (!role.getUsers().isEmpty()) {
            return getOptionalErrorJsonObject("用户组非空");
        }
        RoleService.singletonInstance.delete(role);
        return successOptionalJsonObject;
    }
}
