package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

@Action(name = "changeUserPassword")
public class ChangeUserPasswordAction extends UserJsonResultAction {
    private long qqNumber;
    private String password;
    private String oldPassword;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        User user = optionalUser.orElseThrow();
/*
        if (!(user.getPassword() == null || user.getPassword().isEmpty()) && !SecurityConfig.ENCODER.matches(oldPassword, user.getPassword())) {
            throw new AuthException("wrong password");
        }
*/
        user.setPassword(CheckInApplication.applicationContext.getBean(PasswordEncoder.class).encode(password));
        UserService.singletonInstance.save(user);

        sendUpdateUserToAll(user);
        return successOptionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        Object qqObject = dataMap.get("QQ");
        qqNumber = Long.parseLong((String) qqObject);
        oldPassword = (String) dataMap.get("oldPassword");
        password = (String) dataMap.get("newPassword");
    }
}
