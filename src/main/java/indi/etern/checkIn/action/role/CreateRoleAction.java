package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

@Action("createRole")
public class CreateRoleAction extends BaseAction<CreateRoleAction.Input, MessageOutput> {
    public record Input(String roleType) implements InputData { }
    
    private final WebSocketService webSocketService;
    private final ActionExecutor actionExecutor;
    Role role;
    private final RoleService roleService;
    
    public CreateRoleAction(RoleService roleService, WebSocketService webSocketService, ActionExecutor actionExecutor) {
        this.roleService = roleService;
        this.webSocketService = webSocketService;
        this.actionExecutor = actionExecutor;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("create role");
        final String roleType = context.getInput().roleType;
        if (roleService.existByType(roleType)) {
            context.resolve(MessageOutput.error("Role already exists"));
        } else {
            role = Role.getInstance(roleType, roleService.count());
            
            Permission createdPermission = roleService.save(role);
            
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("type", "addRole");
            LinkedHashMap<String, Object> role = new LinkedHashMap<>();
            role.put("type", this.role.getType());
            role.put("level", this.role.getLevel());
            map.put("role", role);
            webSocketService.sendMessageToAll(map);
            
            if (createdPermission != null) {
                final User currentUser = context.getCurrentUser();
                Role currentUserRole = currentUser.getRole();
                currentUserRole = roleService.findByType(currentUserRole.getType()).orElseThrow();//flush Role.users
                currentUserRole.getPermissions().add(createdPermission);
                actionExecutor.execute(SendPermissionsToUsersAction.class,
                        new SendPermissionsToUsersAction.Input(currentUserRole.getUsers()));
                roleService.save(currentUserRole);
            }
            
            context.resolve(MessageOutput.success("Role created"));
        }
    }
}