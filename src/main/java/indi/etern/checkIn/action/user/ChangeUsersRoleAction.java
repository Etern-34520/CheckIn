package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;

import java.util.*;

@Action("change users role")
public class ChangeUsersRoleAction extends TransactionalAction {
    
    private final UserService userService;
    private final RoleService roleService;
    private final ActionExecutor actionExecutor;
    private List<Number> qqList;
    private String targetRole;
    
    public ChangeUsersRoleAction(UserService userService, RoleService roleService, ActionExecutor actionExecutor) {
        this.userService = userService;
        this.roleService = roleService;
        this.actionExecutor = actionExecutor;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        final Optional<Role> targetOptional = roleService.findByType(targetRole);
        if (targetOptional.isPresent()) {
            final List<User> users = userService.findAllByQQNumber(qqList.stream().map(Number::longValue).toList());
            users.forEach(user -> {
                Map<String,Object> map = new HashMap<>();
                map.put("qq",user.getQQNumber());
                map.put("roleType",targetRole);
                actionExecutor.executeByTypeClass(ChangeUserRoleAction.class,map);
            });
            return Optional.of(getSuccessMap());
        } else {
            return Optional.of(getErrorMap("role not found"));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void initData(Map<String, Object> initData) {
        qqList = (List<Number>) initData.get("qqList");
        targetRole = (String) initData.get("targetRole");
    }
}
