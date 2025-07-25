package indi.etern.checkIn.action.role.permission;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Action("getEnabledPermissionsOfRole")
public class GetEnabledPermissionsOfRoleAction extends BaseAction<GetEnabledPermissionsOfRoleAction.Input, OutputData> {
    private final RoleService roleService;
    
    public GetEnabledPermissionsOfRoleAction(RoleService roleService) {
        super();
        this.roleService = roleService;
    }
    
    public record Input(String roleType) implements InputData {}
    
    public record SuccessOutput(Map<String,List<Permission>> permissionGroups) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        roleService.findByType(context.getInput().roleType).ifPresentOrElse((role) -> {
            Map<String,List<Permission>> permissions = new LinkedHashMap<>();
            role.getPermissions().forEach(permission -> {
                final String name = permission.getGroup().getName();
                permissions.putIfAbsent(name, new ArrayList<>());
                List<Permission> permissionsList = permissions.get(name);
                permissionsList.add(permission);
            });
            context.resolve(new SuccessOutput(permissions));
        }, () -> {
            context.resolve(MessageOutput.error("Role not found"));
        });
    }
}