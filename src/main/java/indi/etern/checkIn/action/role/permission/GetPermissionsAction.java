package indi.etern.checkIn.action.role.permission;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Action("getPermissionsOfRole")
public class GetPermissionsAction extends BaseAction<GetPermissionsAction.Input, GetPermissionsAction.SuccessOutput> {
    public record Input(String roleType) implements InputData {}
    public record SuccessOutput(List<Map<String,Object>> permissionGroups) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    final RoleService roleService;
    
    public GetPermissionsAction(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<GetPermissionsAction.Input, SuccessOutput> context) {
        Role role = roleService.findByType(context.getInput().roleType).orElseThrow();
        final Set<Permission> permissions1 = role.getPermissions();
        final List<PermissionGroup> allPermissionGroups = roleService.findAllPermissionGroup();
        ArrayList<Map<String,Object>> permissionGroups = new ArrayList<>(permissions1.size());
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
        context.resolve(new SuccessOutput(permissionGroups));
    }
}