package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.SecurityConfig;
import indi.etern.checkIn.auth.AuthException;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Optional;

public class ChangeUserPasswordAction extends UserJsonResultAction {
    private final long qqNumber;
    private final String password;
    private final String oldPassword;
    
    public ChangeUserPasswordAction(long qqNumber, String oldPassword, String password) {
        this.qqNumber = qqNumber;
        this.oldPassword = oldPassword;
        this.password = password;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        User user = optionalUser.orElseThrow();
        if (!SecurityConfig.ENCODER.matches(oldPassword, user.getPassword())) {
            throw new AuthException("wrong password");
        }
        user.setPassword(password);
        UserService.singletonInstance.save(user);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "success");
        sendUpdateUserToAll(user);
        
        return Optional.empty();
    }
}
