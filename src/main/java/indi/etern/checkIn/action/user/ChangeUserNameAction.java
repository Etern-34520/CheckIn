package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Map;
import java.util.Optional;

@Action(name = "changeUserName")
public class ChangeUserNameAction extends UserJsonResultAction {
    private long qqNumber;
    private String name;

    @Override
    public String requiredPermissionName() {
        return null;//TODO
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        User user = optionalUser.orElseThrow();
        user.setName(name);
        UserService.singletonInstance.saveAndFlush(user);
        sendUpdateUserToAll(user);
        return successOptionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        Object qqObject = dataMap.get("qq");
        qqNumber = ((Double) qqObject).longValue();
        name = (String) dataMap.get("newName");
    }
}
