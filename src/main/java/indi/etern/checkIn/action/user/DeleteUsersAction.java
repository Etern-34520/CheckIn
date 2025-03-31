package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.service.dao.UserService;

import java.util.List;

@Action("delete users")
public class DeleteUsersAction extends BaseAction1<DeleteUsersAction.Input, MessageOutput> {
    public record Input(List<Long> qqList) implements InputData {}
    
    private final UserService userService;
    
    public DeleteUsersAction(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("delete user");
        userService.deleteAllByQQ(context.getInput().qqList());
        context.resolve(MessageOutput.success("Users deleted"));
    }
}