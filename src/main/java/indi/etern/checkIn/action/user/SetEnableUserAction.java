package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;

@Action("setEnableUser")
public class SetEnableUserAction extends BaseAction<SetEnableUserAction.Input, MessageOutput> {
    public record Input(long qq, boolean enable) implements InputData {
    }
    
    private final UserService userService;
    
    public SetEnableUserAction(UserService userService) {
        this.userService = userService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("change user state");
        final Input input = context.getInput();
        userService.findByQQNumber(input.qq).ifPresentOrElse((user) -> {
            context.requirePermission("operate role " + user.getRole());
            user.setEnabled(input.enable);
            userService.saveAndFlush(user);
            Message<?> message = Message.of("updateUser", user);
            WebSocketService.singletonInstance.sendMessageToAll(message);
            context.resolve(MessageOutput.success("User updated"));
        }, () -> {
            context.resolve(MessageOutput.error("User not exist"));
        });
    }
}