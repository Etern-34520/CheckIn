package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Map;
import java.util.Optional;

@Action(name = "changeUserRole")
public class ChangeUserRoleAction extends UserJsonResultAction{
    private long qqNumber;
    private String roleName;
    
    @Override
    public String requiredPermissionName() {
        return "change role,change role " + roleName;
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<JsonObject> optionalJsonObject = Optional.empty();
        if (UserService.singletonInstance.existsByQQNumber(qqNumber)) {
            User user = UserService.singletonInstance.changeRoleById(qqNumber, roleName);
            optionalJsonObject = successOptionalJsonObject;
            sendUpdateUserToAll(user);
        } else {
            optionalJsonObject = getOptionalErrorJsonObject("user not found");
        }
        return optionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        Object qqObject = dataMap.get("QQ");
        qqNumber = Long.parseLong((String) qqObject);
        roleName = (String) dataMap.get("roleName");
    }
}
