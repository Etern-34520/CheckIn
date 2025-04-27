package indi.etern.checkIn.action.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.BasicOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;
import lombok.SneakyThrows;

import java.util.*;

@Action(value = "send permissions to users",exposed = false)
public class SendPermissionsToUsersAction extends BaseAction<SendPermissionsToUsersAction.Input, BasicOutput> {
    public record Input(Collection<User> users) implements InputData {}
    
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;
    public SendPermissionsToUsersAction(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @SneakyThrows
    public void execute(ExecuteContext<SendPermissionsToUsersAction.Input, BasicOutput> context) {
        Collection<User> users = context.getInput().users();
        Map<String,List<Permission>> permissions = new LinkedHashMap<>();
        for (User user : users) {
            user.getRole().getPermissions().forEach(permission -> {
                final String name = permission.getGroup().getName();
                permissions.putIfAbsent(name, new ArrayList<>());
                List<Permission> permissionsList = permissions.get(name);
                permissionsList.add(permission);
            });
            Message<Map<String,List<Permission>>> message =
                    new Message<>(
                            Message.Type.of("updatePermissions"),
                            permissions
                    );
            webSocketService.sendMessage(message, String.valueOf(user.getQQNumber()));
        }
    }
}
