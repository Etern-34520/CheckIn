package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action("deleteUser")
public class DeleteUserAction extends BaseAction<DeleteUserAction.Input, MessageOutput> {
    public record Input(long qq) implements InputData {}
    
    private final WebSocketService webSocketService;
    private final UserService userService;
    
    public DeleteUserAction(WebSocketService webSocketService, UserService userService) {
        this.webSocketService = webSocketService;
        this.userService = userService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        final Input input = context.getInput();
        final Optional<User> optionalUser = userService.findByQQNumber(input.qq);
        optionalUser.ifPresentOrElse((user) -> {
            if (!user.equals(context.getCurrentUser())) {
                context.requirePermission("delete user");
            }
            userService.delete(user);
            context.resolve(MessageOutput.success("User deleted"));
            Message<User> message = Message.of("deleteUser", user);
            webSocketService.sendMessageToAll(message);
        }, () -> context.resolve(MessageOutput.error("User not found")));
    }
}