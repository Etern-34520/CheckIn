package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;
import indi.etern.checkIn.utils.UUIDv7;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Optional;

@Action("createUser")
public class CreateUserAction extends BaseAction<CreateUserAction.Input, OutputData> {
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final RoleService roleService;

    public CreateUserAction(UserService userService, WebSocketService webSocketService, RoleService roleService) {
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.roleService = roleService;
    }

    @Transactional
    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        context.requirePermission("create user");
        final Input input = context.getInput();
        context.requirePermission("operate role " + input.roleType);
        Optional<LinkedHashMap<String, Object>> result;
        if (userService.existsByQQNumber(input.qq)) {
            context.resolve(MessageOutput.error("user already exists"));
        } else {
            final String initPassword = UUIDv7.randomUUID().toString();
            User newUser = new User(input.name, input.qq, initPassword);
            newUser.setRole(roleService.findByType(input.roleType)
                    .orElse(Role.getInstance(input.roleType, -1)));
            userService.save(newUser);

            context.resolve(new SuccessOutput(initPassword));

            Message<User> message = Message.of("addUser", newUser);
            webSocketService.sendMessageToAll(message);
        }
    }

    public record Input(long qq, String name, String roleType) implements InputData {
    }

    public record SuccessOutput(String initPassword) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
}