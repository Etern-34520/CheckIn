package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.interfaces.ResultContext;
import indi.etern.checkIn.action.role.permission.GetEnabledPermissionsOfRoleAction;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
public class LoginController {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    final JwtTokenProvider jwtTokenProvider;
    final UserService userService;
    final ObjectMapper objectMapper;
    final PasswordEncoder passwordEncoder;
    private final String nameOrQQOrPasswordWrongStr = "{\"result\":\"fail\",\"message\":\"用户名 QQ 或 密码 错误\"}";
    private final String userDisabledStr = "{\"result\":\"fail\",\"message\":\"用户已禁用\"}";
    private final ActionExecutor actionExecutor;
    
    public LoginController(JwtTokenProvider jwtTokenProvider, UserService userService, ObjectMapper objectMapper, PasswordEncoder passwordEncoder, ActionExecutor actionExecutor) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.actionExecutor = actionExecutor;
    }
    
    public static boolean isNumber(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }
    
    private String getResponseOf(User user) throws JsonProcessingException {
        if (!user.isEnabled()) {
            return userDisabledStr;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("result", "success");
        dataMap.put("qq", user.getQQNumber());
        dataMap.put("name", user.getName());
        final String roleType = user.getRole().getType();
        dataMap.put("role", roleType);
        dataMap.put("token", jwtTokenProvider.generateToken(user));
        final ResultContext<OutputData> context = actionExecutor.execute(GetEnabledPermissionsOfRoleAction.class, new GetEnabledPermissionsOfRoleAction.Input(roleType));
        if (context.getOutput() instanceof GetEnabledPermissionsOfRoleAction.SuccessOutput(
                Map<String, List<indi.etern.checkIn.entities.user.Permission>> permissionGroups
        )) {
            dataMap.put("rolePermission", permissionGroups);
        } else {
            throw new IllegalStateException();
        }
        return objectMapper.writeValueAsString(dataMap);
    }
    
    private String checkWithUserName(String name, String password) throws JsonProcessingException {
        final List<User> users = userService.findAllByName(name);
        for (User user : users) {
            if (checkPassword(user, password)) {
                return getResponseOf(user);
            }
        }
        return nameOrQQOrPasswordWrongStr;
    }
    
    @PostMapping(path = "/api/login")
    public String login(@RequestBody Map<String, Object> body) throws JsonProcessingException {
        String usernameOrQQ = (String) body.get("usernameOrQQ");
        String password = (String) body.get("password");
        if (usernameOrQQ == null || password == null) {
            return nameOrQQOrPasswordWrongStr;
        }
        if (isNumber(usernameOrQQ)) {
            long qq = Long.parseLong(usernameOrQQ);
            final Optional<User> optionalUser = userService.findByQQNumber(qq);
            if (optionalUser.isPresent()) {
                final User user = optionalUser.get();
                if (checkPassword(user, password)) {
                    return getResponseOf(user);
                } else {
                    return nameOrQQOrPasswordWrongStr;
                }
            } else {
                return checkWithUserName(usernameOrQQ, password);
            }
        } else {
            return checkWithUserName(usernameOrQQ, password);
        }
    }
    
    private boolean checkPassword(User user, String password) {
        return user.getPassword() == null || passwordEncoder.matches(password, user.getPassword());
    }
}