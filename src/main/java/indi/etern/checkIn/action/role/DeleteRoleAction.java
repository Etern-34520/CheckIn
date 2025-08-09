package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;

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
            
            webSocketService.sendMessageToAll(Message.of("deleteRole", role));
            
            context.resolve(MessageOutput.success("Role deleted"));
        }
    }
}
