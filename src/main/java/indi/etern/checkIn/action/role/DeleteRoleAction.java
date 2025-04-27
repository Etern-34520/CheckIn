package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

@Action("deleteRole")
public class DeleteRoleAction extends BaseAction<DeleteRoleAction.Input, MessageOutput> {
    public record Input(String roleType) implements InputData {}
    
    private final RoleService roleService;
    private final WebSocketService webSocketService;
    
    public DeleteRoleAction(RoleService roleService, WebSocketService webSocketService) {
        super();
        this.roleService = roleService;
        this.webSocketService = webSocketService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<DeleteRoleAction.Input, MessageOutput> context) {
        context.requirePermission("delete role");
        Role role = roleService.findByType(context.getInput().roleType).orElseThrow();
        if (!role.getUsers().isEmpty()) {
            context.resolve(MessageOutput.error("Role is not empty"));
        } else {
            roleService.delete(role);
            
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
            map.put("type","deleteRole");
            LinkedHashMap<String,Object> role1 = new LinkedHashMap<>();
            role1.put("type",role.getType());
            role1.put("level",role.getLevel());
            map.put("role",role1);
            webSocketService.sendMessageToAll(map);
            
            context.resolve(MessageOutput.success("Role deleted"));
        }
    }
}
