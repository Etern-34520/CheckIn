package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.Map;
import java.util.Optional;

@Action(name = "deleteRole")
public class DeleteRoleAction extends TransactionalAction {
    String roleType;
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

    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = (String) dataMap.get("roleType");
    }
}
