package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action("updateRoleLevels")
public class UpdateRoleLevelsAction extends MapResultAction {
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
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<Role> roles = roleService.findAll();
        for (Role role : roles) {
            if (roleTypes.contains(role.getType())) {
                role.setLevel(roleTypes.indexOf(role.getType()));
            }
        }
        roleService.saveAll(roles);
        return successOptionalMap;
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        roleTypes = (List<String>) dataMap.get("levels");
    }
}
