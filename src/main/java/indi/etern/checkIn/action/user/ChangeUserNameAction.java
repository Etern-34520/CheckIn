package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("changeUserName")
public class ChangeUserNameAction extends UserMapResultAction {
    private long qqNumber;
    private String name;

    @Override
    public String requiredPermissionName() {
        return null;//TODO
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        final Optional<User> optionalUser = UserService.singletonInstance.findByQQNumber(qqNumber);
        User user = optionalUser.orElseThrow();
        user.setName(name);
        UserService.singletonInstance.saveAndFlush(user);
        sendUpdateUserToAll(user);
        return successOptionalMap;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = ((Number) dataMap.get("qq")).longValue();
        name = (String) dataMap.get("newName");
    }
}
