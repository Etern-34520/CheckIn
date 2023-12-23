package indi.etern.checkIn.controller.rest;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Pattern;

@RestController
public class Login {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserService userService;
    @Autowired
    Gson gson;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    public static boolean isNumber(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }
    private String checkWithUserName(String name,String password){
        final List<User> users = userService.findAllByName(name);
        for (User user:users){
            if (user.getPassword().equals(password)) {
                if (!user.isEnabled()){
                    return "{\"result\":\"fail\",\"reason\":\"用户已禁用\"}";
                }
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("result","success");
                dataMap.put("qq",user.getQQNumber());
                dataMap.put("name",user.getName());
                dataMap.put("token",jwtTokenProvider.generateToken(user));
                return gson.toJson(dataMap);
            }
        }
        return "{\"result\":\"fail\"}";
    }
    @PostMapping(path = "/login/",params = {"usernameOrQQ","password"})
    public String login(String usernameOrQQ,String password){
        if (isNumber(usernameOrQQ)){
            long qq = Long.parseLong(usernameOrQQ);
            final Optional<User> optionalUser = userService.findByQQNumber(qq);
            if (optionalUser.isPresent()){
                final User user = optionalUser.get();
                if (Objects.equals(user.getPassword(), password)){
                    if (!user.isEnabled()){
                        return "{\"result\":\"fail\",\"reason\":\"用户已禁用\"}";
                    }
                    Map<String,Object> dataMap = new HashMap<>();
                    dataMap.put("result","success");
                    dataMap.put("qq",qq);
                    dataMap.put("name",user.getName());
                    dataMap.put("token",jwtTokenProvider.generateToken(user));
                    return gson.toJson(dataMap);
                } else {
                    return "{\"result\":\"fail\"}";
                }
            } else {
                return checkWithUserName(usernameOrQQ,password);
            }
        } else {
            return checkWithUserName(usernameOrQQ,password);
        }
    }
}