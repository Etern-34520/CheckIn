package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserInfo {
    @Autowired
    private UserService userService;
    private List<User> users;
    public List<User> getUsers() {
        users = userService.findAll();
        return users;
    }
}
