package indi.etern.checkIn.action.user;

import java.util.LinkedHashMap;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Optional;

@Action("setUserState")
public class SetUserStateAction extends TransactionalAction {
    private long qqNumber;
    private boolean enabled;

    @Override
    public String requiredPermissionName() {
        return "change user state";
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (WebSocketService.singletonInstance.isOnline(user)) {
                return getOptionalErrorMap("user is online");
            }
            user.setEnabled(enabled);
            UserService.singletonInstance.save(user);
            UserMapResultAction.sendUpdateUserToAll(user);
        }
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = Long.parseLong((String) dataMap.get("qq"));
        enabled = (boolean) dataMap.get("enabled");
    }
}
