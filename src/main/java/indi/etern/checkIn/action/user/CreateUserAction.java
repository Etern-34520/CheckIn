package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Action(name = "createUser")
public class CreateUserAction extends TransactionalAction {
    
    private String roleType;
    private long qqNumber;
    private String name;

    @Override
    public String requiredPermissionName() {
        return "create user" + (Objects.equals(roleType, "user") ? "" : ",change role,change role " + roleType);
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
            newUser.setRole(RoleService.singletonInstance.findById(roleType).orElse(Role.getInstance(roleType,null)));
            UserService.singletonInstance.save(newUser);
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "success");
                jsonObject.addProperty("initPassword", initPassword);
                result = Optional.of(jsonObject);
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "addUser");
            JsonObject userJsonObject = new JsonObject();
            userJsonObject.addProperty("name", newUser.getName());
            userJsonObject.addProperty("qq", newUser.getQQNumber());
            userJsonObject.addProperty("role", newUser.getRole().getType());
            jsonObject.add("user", userJsonObject);
            WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
        }
        return result;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = (String) dataMap.get("roleType");
        qqNumber = ((Double) dataMap.get("qq")).longValue();
        name = (String) dataMap.get("name");
    }
}
