package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("deleteRole")
public class DeleteRoleAction extends TransactionalAction {
    private final RoleService roleService;
    private final WebSocketService webSocketService;
    String roleType;
    
    public DeleteRoleAction(RoleService roleService, WebSocketService webSocketService) {
        super();
        this.roleService = roleService;
        this.webSocketService = webSocketService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "delete role";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Role role = roleService.findByType(roleType).orElseThrow();
        if (!role.getUsers().isEmpty()) {
            return getOptionalErrorMap("用户组非空");
        }
        roleService.delete(role);
        
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type","deleteRole");
        LinkedHashMap<String,Object> role1 = new LinkedHashMap<>();
        role1.put("type",role.getType());
        role1.put("level",role.getLevel());
        map.put("role",role1);
        webSocketService.sendMessageToAll(map);
        
        return successOptionalMap;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = (String) dataMap.get("roleType");
    }
}
