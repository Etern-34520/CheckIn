package indi.etern.checkIn.controller.rest;

import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
public class Login {
    @Autowired
    UserService userService;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    public static boolean isNumber(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }
    private String checkWithUserName(String name,String password){
        final List<User> users = userService.findAllByName(name);
        for (User user:users){
            if (user.getPassword().equals(password)) return "passed"+user.getQQNumber();
        }
        return "error";
    }
    @PostMapping(path = "/login/",params = {"usernameOrQQ","password"})
    public String login(String usernameOrQQ,String password){
        if (isNumber(usernameOrQQ)){
            long qq = Long.parseLong(usernameOrQQ);
            final Optional<User> optionalUser = userService.findByQQNumber(qq);
            if (optionalUser.isPresent()){
                if (Objects.equals(optionalUser.get().getPassword(), password)){
                    return "passed"+qq;
                } else {
                    return "error";
                }
            } else {
                return checkWithUserName(usernameOrQQ,password);
            }
        } else {
            return checkWithUserName(usernameOrQQ,password);
        }
    }
}
