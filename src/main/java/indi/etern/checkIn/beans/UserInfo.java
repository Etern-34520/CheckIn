package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserInfo {
    private final UserService userService;
    private List<User> users;
    
    public UserInfo(UserService userService) {
        this.userService = userService;
    }
    
    public List<User> getUsers() {
        users = userService.findAll();
        return users;
    }
}
