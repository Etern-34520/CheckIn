package indi.etern.checkIn;

import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
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
@SpringBootTest(classes = CheckInApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {
    @Autowired
    private UserService userService;
    private User etern = new User("etern",941651914,"114514");
    {
        Role role = Role.getInstance("admin");
        etern.setRole(role);
    }
    @Test
    @Transactional
    public void insertUser(){
        userService.save(etern);
    }
    @Test
    @Transactional
    public void getUserByName(){
        final List<User> users = userService.findAllByName("etern");
        User etern1 = users.get(0);
        assert etern1.equals(etern);
        assert users.contains(etern);
    }
    @Test
    @Transactional
    public void getUserByQQNumber(){
        final Optional<User> user = userService.findByQQNumber(941651914);
        assert user.isPresent();
        assert user.get().equals(etern);
        final Optional<User> user1 = userService.findByQQNumber(2797512412L);
    }
}
