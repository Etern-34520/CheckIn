package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Optional;

@Action(name = "deleteUser")
public class DeleteUserAction extends TransactionalAction {
    private long qqNumber;

    @Override
    public String requiredPermissionName() {
        return qqNumber == getCurrentUser().getQQNumber() ? null : "delete user";
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

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = Long.parseLong((String) dataMap.get("QQ"));
    }
}
