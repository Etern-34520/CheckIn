package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;

import java.util.List;

@Action("updateRoleLevels")//Unused currently
public class UpdateRoleLevelsAction extends BaseAction<UpdateRoleLevelsAction.Input, MessageOutput> {
    public record Input(List<String> levels) implements InputData {}
    
    private final RoleService roleService;
    
    public UpdateRoleLevelsAction(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("update role level");
        List<Role> roles = roleService.findAll();
        final List<String> levels = context.getInput().levels;
        for (Role role : roles) {
            if (levels.contains(role.getType())) {
                role.setLevel(levels.indexOf(role.getType()));
            }
        }
        roleService.saveAll(roles);
    }
}
