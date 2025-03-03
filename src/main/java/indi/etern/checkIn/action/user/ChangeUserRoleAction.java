package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.utils.UserUpdateUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("changeUserRole")
public class ChangeUserRoleAction extends TransactionalAction {
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;
    private final RoleService roleService;
    private String roleType;
    private User user;
    
    public ChangeUserRoleAction(UserService userService, TransactionTemplate transactionTemplate, RoleService roleService) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
        this.roleService = roleService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "change role to " + roleType + "," +
                "change role to " + user.getRole().getType();
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<LinkedHashMap<String,Object>> optionalMap;
        transactionTemplate.execute(status -> {
            Role role = roleService.findByType(roleType).orElseThrow();
            user.setRole(role);
            userService.save(user);
            return Boolean.TRUE;
        });
        optionalMap = successOptionalMap;
        UserUpdateUtils.sendUpdateUserToAll(user);
        return optionalMap;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        Object qqObject = dataMap.get("qq");
        long qqNumber = ((Number) qqObject).longValue();
        user = userService.findByQQNumber(qqNumber).orElseThrow();
        roleType = (String) dataMap.get("roleType");
    }
}
