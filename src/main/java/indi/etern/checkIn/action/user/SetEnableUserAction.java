package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("setEnableUser")
public class SetEnableUserAction extends UserMapResultAction {
    private long qqNumber;
    private boolean enable;
    private final UserService userService;
    
    public SetEnableUserAction(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "change user state";
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<LinkedHashMap<String,Object>> optionalMap;
        final Optional<User> userOptional = userService.findByQQNumber(qqNumber);
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            user.setEnabled(enable);
            userService.saveAndFlush(user);
            optionalMap = successOptionalMap;
            sendUpdateUserToAll(user);
        } else {
            optionalMap = getOptionalErrorMap("user not found");
        }
        return optionalMap;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        Object qqObject = dataMap.get("qq");
        qqNumber = ((Number) qqObject).longValue();
        enable = (boolean) dataMap.get("enable");
    }
    
}
