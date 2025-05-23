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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Action("changeUserPassword")
public class ChangeUserPasswordAction extends BaseAction<ChangeUserPasswordAction.Input, MessageOutput> {
    public record Input(String oldPassword, String newPassword) implements InputData {}
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    public ChangeUserPasswordAction(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        User user = context.getCurrentUser();
        final Input input = context.getInput();
        if (!(user.getPassword() == null || user.getPassword().isEmpty()) && !passwordEncoder.matches(input.oldPassword, user.getPassword())) {
            context.resolve(MessageOutput.error("Previous password incorrect"));
        } else {
            user.setPassword(passwordEncoder.encode(input.newPassword));
            userService.saveAndFlush(user);
            Message<?> message = Message.of("updateUser", user);
            WebSocketService.singletonInstance.sendMessageToAll(message);
            context.resolve(MessageOutput.success("Password changed successfully"));
        }
    }
}