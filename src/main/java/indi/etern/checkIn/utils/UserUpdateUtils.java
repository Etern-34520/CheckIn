package indi.etern.checkIn.utils;

import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.LinkedHashMap;

public class UserUpdateUtils {
    public static void sendUpdateUserToAll(User user) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", "updateUser");
        map.put("user", user.toMap());
        WebSocketService.singletonInstance.sendMessageToAll(map);
    }
}
