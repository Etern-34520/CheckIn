package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Action("change users role")
public class ChangeUsersRoleAction extends BaseAction<ChangeUsersRoleAction.Input, MessageOutput> {
    public record Input(List<Long> qqList, String targetRole) implements InputData {}
    
    private final UserService userService;
    private final RoleService roleService;
    private final ActionExecutor actionExecutor;
    
    public ChangeUsersRoleAction(UserService userService, RoleService roleService, ActionExecutor actionExecutor) {
        this.userService = userService;
        this.roleService = roleService;
        this.actionExecutor = actionExecutor;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        final Input input = context.getInput();
        roleService.findByType(input.targetRole).ifPresentOrElse((targetTole) -> {
            final List<User> users = userService.findAllByQQNumber(input.qqList);
            users.forEach(user -> {
                var input1 = new ChangeUserRoleAction.Input(user.getQQNumber(), input.targetRole);
                actionExecutor.execute(ChangeUserRoleAction.class,input1);
            });
            context.resolve(MessageOutput.success("Users role changed"));
        }, () -> context.resolve(MessageOutput.error("Users role not found")));
    }
}