package indi.etern.checkIn.action.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Optional;

@Action(name = "getAllUsers")
public class GetAllUsersAction extends UserJsonResultAction{

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        JsonObject result = new JsonObject();
        JsonArray userList = new JsonArray();
        UserService.singletonInstance.findAll().forEach(user -> {
            JsonObject userInfo = new JsonObject();
            userInfo.addProperty("qq", user.getQQNumber());
            userInfo.addProperty("name", user.getName());
            userInfo.addProperty("role", user.getRole().getType());
            userInfo.addProperty("enabled", user.isEnabled());
            userList.add(userInfo);
        });
        result.add("users", userList);
        return Optional.of(result);
    }
}
