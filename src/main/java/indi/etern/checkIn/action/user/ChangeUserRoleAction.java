package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.Optional;

@Action("changeUserRole")
public class ChangeUserRoleAction extends BaseAction<ChangeUserRoleAction.Input, MessageOutput> {
    public record Input(long qq, String roleType) implements InputData {
    }
    
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;
    private final RoleService roleService;
    
    public ChangeUserRoleAction(UserService userService, TransactionTemplate transactionTemplate, RoleService roleService) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
        this.roleService = roleService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        final Input input = context.getInput();
        User user = userService.findByQQNumber(input.qq).orElseThrow();
        context.requirePermission("change role to " + input.roleType);
        context.requirePermission("change role to " + user.getRole().getType());
        Optional<LinkedHashMap<String, Object>> optionalMap;
        Role role = roleService.findByType(input.roleType).orElseThrow();
        user.setRole(role);
        userService.save(user);
        Message<?> message = Message.of("updateUser", user);
        WebSocketService.singletonInstance.sendMessageToAll(message);
        context.resolve(MessageOutput.success("Role of user changed successfully"));
    }
}