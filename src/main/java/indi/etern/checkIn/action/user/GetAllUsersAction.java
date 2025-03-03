package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.UserService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getAllUsers")
public class GetAllUsersAction extends TransactionalAction {

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        LinkedHashMap<String,Object> result = getSuccessMap();
        ArrayList<Object> userList = new ArrayList<>();
        UserService.singletonInstance.findAll().forEach(user -> {
            LinkedHashMap<String,Object> userInfo = new LinkedHashMap<>();
            userInfo.put("qq", user.getQQNumber());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getRole().getType());
            userInfo.put("enabled", user.isEnabled());
            userList.add(userInfo);
        });
        result.put("users", userList);
        return Optional.of(result);
    }
}
