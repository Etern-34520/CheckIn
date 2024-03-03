package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.SecurityConfig;
import indi.etern.checkIn.auth.AuthException;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class ChangeUserPasswordAction extends UserJsonResultAction {
    private final long qqNumber;
    private final String password;
    private final String oldPassword;
    private User user;

    public ChangeUserPasswordAction(long qqNumber, String oldPassword, String password) {
        this.qqNumber = qqNumber;
        this.oldPassword = oldPassword;
        this.password = password;
        this.logging = false;
    }

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        User user = optionalUser.orElseThrow();
        if (!(user.getPassword() == null || user.getPassword().isEmpty()) && !SecurityConfig.ENCODER.matches(oldPassword, user.getPassword())) {
            throw new AuthException("wrong password");
        }
        user.setPassword(CheckInApplication.applicationContext.getBean(PasswordEncoder.class).encode(password));
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
