package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action(name = "savePermission")
public class SavePermissionAction extends TransactionalAction {
    Role role;
    List<String> enables;
    
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

    @Override
    public void initData(Map<String, Object> dataMap) {
        role = RoleService.singletonInstance.findByType((String) dataMap.get("roleType")).orElseThrow();
        //noinspection unchecked
        enables = (List<String>) dataMap.get("enables");
    }
}
