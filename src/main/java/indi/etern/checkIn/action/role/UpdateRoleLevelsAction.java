package indi.etern.checkIn.action.role;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.JsonResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action(name = "updateRoleLevels")
public class UpdateRoleLevelsAction extends JsonResultAction {
    private final RoleService roleService;
    private List<String> roleTypes;
    
    public UpdateRoleLevelsAction(RoleService roleService) {
        super();
        this.roleService = roleService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        List<Role> roles = roleService.findAll();
        for (Role role : roles) {
            if (roleTypes.contains(role.getType())) {
                role.setLevel(roleTypes.indexOf(role.getType()));
            }
        }
        roleService.saveAll(roles);
        return successOptionalJsonObject;
    }
    
    @Override
    public void initData(Map<String, Object> dataObj) {
        //noinspection unchecked
        roleTypes = (List<String>) dataObj.get("levels");
    }
}
