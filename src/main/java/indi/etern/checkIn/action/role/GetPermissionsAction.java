package indi.etern.checkIn.action.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.JsonResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.*;

@Action(name = "getPermissionsOfRole")
public class GetPermissionsAction extends JsonResultAction {
    String roleType;
    final RoleService roleService;
    
    public GetPermissionsAction(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Role role = roleService.findByType(roleType).orElseThrow();
        JsonObject result = new JsonObject();
        final Set<Permission> permissions1 = role.getPermissions();
        final List<PermissionGroup> allPermissionGroups = roleService.findAllPermissionGroup();
        JsonArray permissionGroups = new JsonArray(permissions1.size());
        for (PermissionGroup permissionGroup : allPermissionGroups) {
            JsonObject permissionGroup1 = new JsonObject();
            JsonArray groupPermissions = new JsonArray();
            
            final List<Permission> permissions = permissionGroup.getPermissions();
            permissions.sort(Comparator.comparing(Permission::getName));
            for (Permission permission : permissions) {
                JsonObject permission1 = new JsonObject();
                permission1.addProperty("name",permission.getName());
                permission1.addProperty("description",permission.getDescription());
                permission1.addProperty("enabled",permissions1.contains(permission));
                groupPermissions.add(permission1);
            }
            
            permissionGroup1.addProperty("name",permissionGroup.getName());
            permissionGroup1.addProperty("description",permissionGroup.getDescription());
            permissionGroup1.add("permissions",groupPermissions);
            
            permissionGroups.add(permissionGroup1);
        }
        result.add("permissionGroups",permissionGroups);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> dataObj) {
        roleType = dataObj.get("roleType").toString();
    }
}
