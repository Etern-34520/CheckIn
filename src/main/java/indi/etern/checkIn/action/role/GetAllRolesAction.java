package indi.etern.checkIn.action.role;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.RoleService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Action("getAllRoles")
public class GetAllRolesAction extends BaseAction<NullInput, GetAllRolesAction.SuccessOutput> {
    public record SuccessOutput(ArrayList<Map<String, Object>> roles) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    final private RoleService roleService;
    
    public GetAllRolesAction(RoleService roleService) {
        this.roleService = roleService;
    }

    @Transactional
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        ArrayList<Map<String,Object>> roleList = new ArrayList<>();
        final List<Role> roles = roleService.findAll();
        roles.sort(Comparator.comparingInt(Role::getLevel));
        roles.forEach(role -> {
            LinkedHashMap<String,Object> roleInfo = new LinkedHashMap<>();
            roleInfo.put("type", role.getType());
            roleInfo.put("level", role.getLevel());
            roleList.add(roleInfo);
        });
        context.resolve(new SuccessOutput(roleList));
    }
}
