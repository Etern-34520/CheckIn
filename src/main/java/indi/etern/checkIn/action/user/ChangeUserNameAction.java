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

@Action("changeUserName")
public class ChangeUserNameAction extends BaseAction<ChangeUserNameAction.Input, MessageOutput> {
    private final UserService userService;
    
    public ChangeUserNameAction(UserService userService) {
        this.userService = userService;
    }
    
    public record Input(long qq, String newName) implements InputData {}
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        final Input input = context.getInput();
        if (input.qq != context.getCurrentUser().getQQNumber()) {
            context.requirePermission("change user name");
            context.requirePermission("operate user " + context.getCurrentUser().getRole().getType());
        }
        final Optional<User> optionalUser = userService.findByQQNumber(input.qq);
        User user = optionalUser.orElseThrow();
        user.setName(input.newName);
        userService.saveAndFlush(user);
        Message<?> message = Message.of("updateUser", user);
        WebSocketService.singletonInstance.sendMessageToAll(message);
        context.resolve(MessageOutput.success("Renamed user successfully"));
    }
}
