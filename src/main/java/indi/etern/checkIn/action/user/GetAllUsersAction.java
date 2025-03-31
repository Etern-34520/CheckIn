package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.List;

@Action("getAllUsers")
public class GetAllUsersAction extends BaseAction1<NullInput, GetAllUsersAction.SuccessOutput> {
    public record SuccessOutput(List<User> users) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    private final UserService userService;
    
    public GetAllUsersAction(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        final List<User> users = userService.findAll();
        context.resolve(new SuccessOutput(users));
    }
}
