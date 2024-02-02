package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CreateUserAction extends TransactionalAction {
    
    private final String roleName;
    private final long qqNumber;
    private final String name;
    private User newUser;
    
    public CreateUserAction(long qqNumber, String name, String roleName) {
        this.qqNumber = qqNumber;
        this.name = name;
        this.roleName = roleName;
    }
    
    @Override
    public String requiredPermissionName() {
        return "create user" + (Objects.equals(roleName, "user") ? "" : ",change role,change role " + roleName);
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<JsonObject> result;
        if (UserService.singletonInstance.existsByQQNumber(qqNumber)) {
            result = getOptionalErrorJsonObject("user already exists");
        } else {
            UserService.singletonInstance.unbindAndDeleteById(qqNumber);
            final String initPassword = UUID.randomUUID().toString();
            User newUser = new User(name, qqNumber, initPassword);
            newUser.setRole(Role.getInstance(roleName));
            UserService.singletonInstance.save(newUser);
            this.newUser = newUser;
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "success");
                jsonObject.addProperty("initPassword", initPassword);
                result = Optional.of(jsonObject);
            }
        }
        return result;
    }
    
    @Override
    public void afterAction() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "addUser");
        JsonObject userJsonObject = new JsonObject();
        userJsonObject.addProperty("name", newUser.getName());
        userJsonObject.addProperty("qq", newUser.getQQNumber());
        userJsonObject.addProperty("role", newUser.getRole().getType());
        jsonObject.add("user", userJsonObject);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}
