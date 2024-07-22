package indi.etern.checkIn;

import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.PermissionGroup;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    private User etern = new User("etern", 941651914, "114514");
    
    {
        Role role = Role.getInstance("admin",null);
        etern.setRole(role);
    }
    
    @Test
    @Transactional
    public void insertUser() {
        userService.save(etern);
    }
    
    @Test
    @Transactional
    public void getUserByName() {
        final List<User> users = userService.findAllByName("etern");
        User etern1 = users.get(0);
        assert etern1.equals(etern);
        assert users.contains(etern);
    }
    
    @Test
    @Transactional
    public void getUserByQQNumber() {
        final Optional<User> user = userService.findByQQNumber(941651914);
        assert user.isPresent();
        assert user.get().equals(etern);
        final Optional<User> user1 = userService.findByQQNumber(2797512412L);
    }
    
    @Test
    public void testPermission() {
        testPermission1();
        testPermission2();
    }
    
    @Test
    public void testPermission1() {
        PermissionGroup manageUser = new PermissionGroup("manage user");
        manageUser.setDescription("管理用户");
        String[] permissionNames = {"create user", "delete user", "change user state", "change role", "delete and edit permission"};
        String[] permissionDescription = {"添加用户", "删除用户", "启/禁用用户", "更改用户组", "删除和修改组权限"};
        
        int index = 0;
        for (String name : permissionNames) {
            Permission permission = new Permission(name);
            permission.setDescription(permissionDescription[index]);
            manageUser.add(permission);
            index++;
        }
        roleService.savePermissionGroup(manageUser);
    }
    
    @Test
    public void testPermission2() {
        PermissionGroup manageUser = new PermissionGroup("question");
        manageUser.setDescription("题目");
        String[] permissionNames = {
                "enable and disable question",
                "delete others question",
                "edit others question",
                "create edit and delete owns question",
                "create partition",
                "delete partition",
                "edit partition name"
        };
        String[] permissionDescription = {
                "启/禁用题目",
                "删除他人的题目",
                "编辑他人的题目",
                "创建,编辑和删除自己的题目",
                "创建分区",
                "删除分区",
                "编辑分区名称"
        };
        int index = 0;
        for (String name : permissionNames) {
            Permission permission = new Permission(name);
            permission.setDescription(permissionDescription[index]);
            manageUser.add(permission);
            index++;
        }
        roleService.savePermissionGroup(manageUser);
    }
    
//    @Test
//    public void testEncoding() {
//        System.out.println(SecurityConfig.ENCODER.encode("114514"));
//    }
}
