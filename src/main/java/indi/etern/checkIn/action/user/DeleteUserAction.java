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
    private long qqNumber;
    
    public DeleteUserAction(WebSocketService webSocketService) {
        super();
        this.webSocketService = webSocketService;
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
            UserService.singletonInstance.unbindAndDelete(user);
            {
                LinkedHashMap<String,Object> map = new LinkedHashMap<>();
                map.put("type", "deleteUser");
                map.put("qq", qqNumber);
                webSocketService.sendMessageToAll(map);
            }
        }
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = ((Number) dataMap.get("qq")).longValue();
    }
}
