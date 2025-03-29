package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.utils.UserUpdateUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action("changeUserName")
public class ChangeUserNameAction extends BaseAction1<ChangeUserNameAction.Input, MessageOutput> {
    private UserService userService;
    
    public ChangeUserNameAction(UserService userService) {
        this.userService = userService;
    }
    
    public record Input(long qqNumber, String newName) implements InputData {}
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        final Input input = context.getInput();
        if (input.qqNumber != context.getCurrentUser().getQQNumber()) {
            context.requirePermission("change user name");
        }
        final Optional<User> optionalUser = userService.findByQQNumber(input.qqNumber);
        User user = optionalUser.orElseThrow();
        user.setName(input.newName);
        userService.saveAndFlush(user);
        UserUpdateUtils.sendUpdateUserToAll(user);
        context.resolve(MessageOutput.success("Renamed user successfully"));
    }
}
