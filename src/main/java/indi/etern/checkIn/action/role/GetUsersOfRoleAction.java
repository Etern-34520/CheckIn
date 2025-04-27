package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Action("getUsersOfRole")
public class GetUsersOfRoleAction extends BaseAction<GetUsersOfRoleAction.Input, GetUsersOfRoleAction.SuccessOutput> {
    public record Input(String roleType) implements InputData {}
    public record SuccessOutput(ArrayList<Map<String,Object>> users) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final UserService userService;
    public GetUsersOfRoleAction(UserService userService) {
        this.userService = userService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        ArrayList<Map<String,Object>> userList = new ArrayList<>();
        userService.findAllByRoleType(context.getInput().roleType).forEach(user -> {
            LinkedHashMap<String,Object> userInfo = new LinkedHashMap<>();
            userInfo.put("qq", user.getQQNumber());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getRole().getType());
            userInfo.put("enabled", user.isEnabled());
            userList.add(userInfo);
        });
        context.resolve(new SuccessOutput(userList));
    }
}
