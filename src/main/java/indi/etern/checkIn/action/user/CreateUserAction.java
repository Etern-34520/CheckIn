package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.*;

@Action("createUser")
public class CreateUserAction extends TransactionalAction {
    
    private String roleType;
    private long qqNumber;
    private String name;

    @Override
    public String requiredPermissionName() {
        return "create user,change role to " + roleType;
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<LinkedHashMap<String,Object>> result;
        if (UserService.singletonInstance.existsByQQNumber(qqNumber)) {
            result = getOptionalErrorMap("user already exists");
        } else {
//            UserService.singletonInstance.unbindAndDeleteById(qqNumber);
            final String initPassword = UUID.randomUUID().toString();
            User newUser = new User(name, qqNumber, initPassword);
            newUser.setRole(RoleService.singletonInstance.findByType(roleType).orElse(Role.getInstance(roleType,-1)));
            UserService.singletonInstance.save(newUser);
            {
                LinkedHashMap<String,Object> map = new LinkedHashMap<>();
                map.put("type", "success");
                map.put("initPassword", initPassword);
                result = Optional.of(map);
            }
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
            map.put("type", "addUser");
            LinkedHashMap<String,Object> userMap = new LinkedHashMap<>();
            userMap.put("name", newUser.getName());
            userMap.put("qq", newUser.getQQNumber());
            userMap.put("role", newUser.getRole().getType());
            map.put("user", userMap);
            WebSocketService.singletonInstance.sendMessageToAll(map);
        }
        return result;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = (String) dataMap.get("roleType");
        qqNumber = ((Number) dataMap.get("qq")).longValue();
        name = (String) dataMap.get("name");
    }
}
