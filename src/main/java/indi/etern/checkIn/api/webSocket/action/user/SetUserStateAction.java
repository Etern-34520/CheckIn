package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Optional;

public class SetUserStateAction extends JsonResultAction {
    private final long qqNumber;
    private final boolean enabled;
    
    public SetUserStateAction(long qqNumber, boolean enabled) {
        this.qqNumber = qqNumber;
        this.enabled = enabled;
    }
    
    @Override
    public String requiredPermissionName() {
        return "change user state";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (WebSocketService.singletonInstance.isOnline(user)) {
                return getOptionalErrorJsonObject("user is online");
            }
            user.setEnabled(enabled);
            UserService.singletonInstance.save(user);
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "updateUser");
                jsonObject.add("user", user.toJsonObject());
                WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
            }
        }
        return Optional.empty();
    }
}
