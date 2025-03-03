package indi.etern.checkIn.action.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;
import lombok.SneakyThrows;

import java.util.*;

@Action(value = "send permissions to users",exposed = false)
public class SendPermissionsToUsersAction extends BaseAction<Void, Collection<User>> {
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;
    private Collection<User> users;
    
    public SendPermissionsToUsersAction(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    @SneakyThrows
    protected Optional<Void> doAction() throws Exception {
        LinkedHashMap<String,Object> message = new LinkedHashMap<>();
        message.put("type", "updatePermissions");
        Map<String,Object> permissions = new LinkedHashMap<>();
        for (User user : users) {
            user.getRole().getPermissions().forEach(permission -> {
                final String name = permission.getGroup().getName();
                permissions.putIfAbsent(name, new ArrayList<Permission>());
                //noinspection unchecked
                List<Permission> permissionsList = (List<Permission>) permissions.get(name);
                permissionsList.add(permission);
            });
            message.put("permissions", permissions);
            webSocketService.sendMessage(objectMapper.writeValueAsString(message), String.valueOf(user.getQQNumber()));
        }
        return Optional.empty();
    }
    
    @Override
    public void initData(Collection<User> users) {
        this.users = users;
    }
}
