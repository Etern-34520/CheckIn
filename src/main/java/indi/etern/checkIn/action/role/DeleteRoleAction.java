package indi.etern.checkIn.action.role;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Optional;

@Action(name = "deleteRole")
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
    protected Optional<JsonObject> doAction() throws Exception {
        Role role = roleService.findByType(roleType).orElseThrow();
        if (!role.getUsers().isEmpty()) {
            return getOptionalErrorJsonObject("用户组非空");
        }
        roleService.delete(role);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type","deleteRole");
        JsonObject role1 = new JsonObject();
        role1.addProperty("type",role.getType());
        role1.addProperty("level",role.getLevel());
        jsonObject.add("role",role1);
        webSocketService.sendMessageToAll(jsonObject);
        
        return successOptionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        roleType = (String) dataMap.get("roleType");
    }
}
