package indi.etern.checkIn.action.role.permission;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.role.SendPermissionsToUsersAction;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.service.dao.RoleService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Action("savePermissionsOfRole")
public class SavePermissionAction extends BaseAction<SavePermissionAction.Input, MessageOutput> {
    public record Input(String roleType, List<String> enables) implements InputData {}
    
    final RoleService roleService;
    private final ActionExecutor actionExecutor;
    
    public SavePermissionAction(RoleService roleService, ActionExecutor actionExecutor) {
        this.roleService = roleService;
        this.actionExecutor = actionExecutor;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        synchronized (roleService) {
            context.requirePermission("edit permission");
            final Input input = context.getInput();
            roleService.findByType(input.roleType).ifPresentOrElse((role) -> {
                List<Permission> enabled = new ArrayList<>();
                for (String permissionName : input.enables) {
                    enabled.add(roleService.findPermission(permissionName).orElseThrow());
                }
                role.getPermissions().clear();
                role.getPermissions().addAll(enabled);
                roleService.save(role);
                actionExecutor.execute(SendPermissionsToUsersAction.class,
                        new SendPermissionsToUsersAction.Input(role.getUsers()));
                context.resolve(MessageOutput.success("Permissions saved successfully"));
            }, () -> {
                context.resolve(MessageOutput.error("Role not found"));
            });
        }
    }
}