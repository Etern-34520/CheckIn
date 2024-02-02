package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SavePermissionAction extends TransactionalAction {
    Role role;
    List<String> enables;
    
    public SavePermissionAction(Role role, List<String> enables) {
        this.role = role;
        this.enables = enables;
    }
    
    @Override
    public String requiredPermissionName() {
        return "edit permission";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        List<Permission> enabled = new ArrayList<>();
        for (String permissionName : enables) {
            enabled.add(RoleService.singletonInstance.findPermission(permissionName).orElseThrow());
        }
        role.getPermissions().clear();
        role.getPermissions().addAll(enabled);
        RoleService.singletonInstance.save(role);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type","savePermissionCallback");
        jsonObject.addProperty("message","success");
        return Optional.of(jsonObject);
    }
}
