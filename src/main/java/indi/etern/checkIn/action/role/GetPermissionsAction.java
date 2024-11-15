package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.*;

@Action("getPermissionsOfRole")
public class GetPermissionsAction extends MapResultAction {
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
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Role role = roleService.findByType(roleType).orElseThrow();
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        final Set<Permission> permissions1 = role.getPermissions();
        final List<PermissionGroup> allPermissionGroups = roleService.findAllPermissionGroup();
        ArrayList<Object> permissionGroups = new ArrayList<>(permissions1.size());
        for (PermissionGroup permissionGroup : allPermissionGroups) {
            LinkedHashMap<String,Object> permissionGroup1 = new LinkedHashMap<>();
            ArrayList<Object> groupPermissions = new ArrayList<>();
            
            final List<Permission> permissions = permissionGroup.getPermissions();
            permissions.sort(Comparator.comparing(Permission::getName));
            for (Permission permission : permissions) {
                LinkedHashMap<String,Object> permission1 = new LinkedHashMap<>();
                permission1.put("name",permission.getName());
                permission1.put("description",permission.getDescription());
                permission1.put("enabled",permissions1.contains(permission));
                groupPermissions.add(permission1);
            }
            
            permissionGroup1.put("name",permissionGroup.getName());
            permissionGroup1.put("description",permissionGroup.getDescription());
            permissionGroup1.put("permissions",groupPermissions);
            
            permissionGroups.add(permissionGroup1);
        }
        result.put("permissionGroups",permissionGroups);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = dataMap.get("roleType").toString();
    }
}
