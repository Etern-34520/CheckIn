package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.utils.UserUpdateUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("changeUserPassword")
public class ChangeUserPasswordAction extends TransactionalAction {
    private final UserService userService;
    private String password;
    private String oldPassword;
    private final PasswordEncoder passwordEncoder;
    
    public ChangeUserPasswordAction(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        User user = getCurrentUser();
        if (!(user.getPassword() == null || user.getPassword().isEmpty()) && !passwordEncoder.matches(oldPassword, user.getPassword())) {
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
            map.put("type", "fail");
            map.put("message", "wrong password");
            map.put("failureType", "previousPasswordIncorrect");
            return Optional.of(map);
//            throw new AuthException("wrong password");
        }
        user.setPassword(passwordEncoder.encode(password));
        userService.saveAndFlush(user);//FIXME
        
        UserUpdateUtils.sendUpdateUserToAll(user);
        return successOptionalMap;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        oldPassword = (String) dataMap.get("oldPassword");
        password = (String) dataMap.get("newPassword");
    }
}
