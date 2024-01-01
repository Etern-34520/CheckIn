package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;

public abstract class UserJsonResultAction extends JsonResultAction {
    protected static void sendUpdateUserToAll(User user) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "updateUser");
        jsonObject.add("user", user.toJsonObject());
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}
