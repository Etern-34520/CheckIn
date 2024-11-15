package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("createRole")
public class CreateRoleAction extends TransactionalAction {
    private final WebSocketService webSocketService;
    Role role;
    private final RoleService roleService;
    
    public CreateRoleAction(RoleService roleService, WebSocketService webSocketService) {
        this.roleService = roleService;
        this.webSocketService = webSocketService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "create role";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        roleService.save(role);
        
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type","addRole");
        LinkedHashMap<String,Object> role = new LinkedHashMap<>();
        role.put("type",this.role.getType());
        role.put("level",this.role.getLevel());
        map.put("role",role);
        webSocketService.sendMessageToAll(map);
        
        return successOptionalMap;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        role = Role.getInstance((String) dataMap.get("roleType"), roleService.count());
    }
}
