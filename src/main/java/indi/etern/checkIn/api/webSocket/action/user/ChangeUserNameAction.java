package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Optional;

public class ChangeUserNameAction extends UserJsonResultAction {
    private final long qqNumber;
    private final String name;
    private User user;

    public ChangeUserNameAction(long qqNumber, String name) {
        this.qqNumber = qqNumber;
        this.name = name;
    }

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        User user = optionalUser.orElseThrow();
        user.setName(name);
        UserService.singletonInstance.save(user);
        this.user = user;
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
        if (user != null)
            sendUpdateUserToAll(user);
    }
}
