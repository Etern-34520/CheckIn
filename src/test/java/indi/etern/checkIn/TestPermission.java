package indi.etern.checkIn;


import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.service.dao.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestPermission {
    @Autowired
    private RoleService roleService;
    
    @Test
    public void testInsert() {
        Permission permission = new Permission("test permission");
        PermissionGroup permissionGroup = roleService.findPermissionGroupByName("manage user");
        permission.setGroup(permissionGroup);
        roleService.savePermission(permission);
    }
}
