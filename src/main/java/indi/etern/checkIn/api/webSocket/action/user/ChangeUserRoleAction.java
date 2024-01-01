package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Optional;

public class ChangeUserRoleAction extends UserJsonResultAction{
    private final long qqNumber;
    private final String roleName;
    
    public ChangeUserRoleAction(long qqNumber, String roleName) {
        this.qqNumber = qqNumber;
        this.roleName = roleName;
    }
    
    @Override
    public String requiredPermissionName() {
        return "change role";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<JsonObject> optionalJsonObject = Optional.empty();
        if (UserService.singletonInstance.existsByQQNumber(qqNumber)) {
            User user = UserService.singletonInstance.changeRoleById(qqNumber, roleName);
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "success");
            optionalJsonObject = Optional.of(jsonObject);
            sendUpdateUserToAll(user);
        } else {
            optionalJsonObject = getOptionalErrorJsonObject("user not found");
        }
        return optionalJsonObject;
    }
}
