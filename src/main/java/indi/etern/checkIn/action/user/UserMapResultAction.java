package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.LinkedHashMap;

public abstract class UserMapResultAction extends TransactionalAction {
    protected static void sendUpdateUserToAll(User user) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", "updateUser");
        map.put("user", user.toMap());
        WebSocketService.singletonInstance.sendMessageToAll(map);
    }
}
