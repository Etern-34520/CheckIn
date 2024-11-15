package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("changeUserRole")
public class ChangeUserRoleAction extends UserMapResultAction {
    private long qqNumber;
    private String roleType;
    
    @Override
    public String requiredPermissionName() {
        return "change role,change role " + roleType;
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<LinkedHashMap<String,Object>> optionalMap;
        if (UserService.singletonInstance.existsByQQNumber(qqNumber)) {
            User user = UserService.singletonInstance.changeRoleById(qqNumber, roleType);
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
        qqNumber = ((Double) qqObject).longValue();
        roleType = (String) dataMap.get("roleType");
    }
}
