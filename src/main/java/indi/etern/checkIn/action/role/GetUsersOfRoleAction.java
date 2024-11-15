package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.user.UserMapResultAction;
import indi.etern.checkIn.service.dao.UserService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getUsersOfRole")
public class GetUsersOfRoleAction extends UserMapResultAction {
    String roleType;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        ArrayList<Object> userList = new ArrayList<>();
        UserService.singletonInstance.findAllByRoleType(roleType).forEach(user -> {
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
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = dataMap.get("roleType").toString();
    }
}
