package indi.etern.checkIn.action.role;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Optional;

@Action(name = "createRole")
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
    protected Optional<JsonObject> doAction() throws Exception {
        roleService.save(role);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type","addRole");
        JsonObject role = new JsonObject();
        role.addProperty("type",this.role.getType());
        role.addProperty("level",this.role.getLevel());
        jsonObject.add("role",role);
        webSocketService.sendMessageToAll(jsonObject);
        
        return successOptionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        role = Role.getInstance((String) dataMap.get("roleType"), roleService.count());
    }
}
