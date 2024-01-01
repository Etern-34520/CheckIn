package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Optional;

public class DeleteUserAction extends JsonResultAction {
    private final long qqNumber;
    
    public DeleteUserAction(long qqNumber) {
        this.qqNumber = qqNumber;
    }
    
    @Override
    public String requiredPermissionName() {
        return "delete user";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserService.singletonInstance.unbindAndDelete(user);
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "deleteUser");
                jsonObject.addProperty("QQ", qqNumber);
                WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
            }
        }
        return Optional.empty();
    }
}
