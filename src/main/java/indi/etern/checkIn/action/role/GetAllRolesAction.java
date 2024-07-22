package indi.etern.checkIn.action.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.user.UserJsonResultAction;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Action(name = "getAllRoles")
public class GetAllRolesAction extends UserJsonResultAction {
    final private RoleService roleService;
    
    public GetAllRolesAction(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        JsonObject result = new JsonObject();
        JsonArray roleList = new JsonArray();
        final List<Role> roles = roleService.findAll();
        roles.sort(Comparator.comparingInt(Role::getLevel));
        roles.forEach(role -> {
            JsonObject roleInfo = new JsonObject();
            roleInfo.addProperty("type", role.getType());
            roleInfo.addProperty("level", role.getLevel());
            roleList.add(roleInfo);
        });
        result.add("roles", roleList);
        return Optional.of(result);
    }
}
