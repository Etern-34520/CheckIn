package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("deleteUser")
public class DeleteUserAction extends TransactionalAction {
    private final WebSocketService webSocketService;
    private final UserService userService;
    private long qqNumber;
    
    public DeleteUserAction(WebSocketService webSocketService, UserService userService) {
        this.webSocketService = webSocketService;
        this.userService = userService;
    }
    
    @Override
    public String requiredPermissionName() {
        return qqNumber == getCurrentUser().getQQNumber() ? null : "delete user";
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            final String roleName = user.getRole().getType();
            userService.delete(user);
            {
                LinkedHashMap<String,Object> map = new LinkedHashMap<>();
                map.put("type", "deleteUser");
                map.put("name", user.getName());
                map.put("role", roleName);
                map.put("qq", qqNumber);
                webSocketService.sendMessageToAll(map);
            }
        }
        return Optional.of(getSuccessMap());
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = ((Number) dataMap.get("qq")).longValue();
    }
}
