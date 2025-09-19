package indi.etern.checkIn.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import indi.etern.checkIn.entities.user.OAuth2Binding;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.OAuth2BindingRepository;
import indi.etern.checkIn.repositories.RoleRepository;
import indi.etern.checkIn.repositories.UserRepository;
import indi.etern.checkIn.service.web.OAuth2Service;
import indi.etern.checkIn.service.web.WebSocketService;
import indi.etern.checkIn.throwable.entity.UserExistsException;
import indi.etern.checkIn.throwable.exam.ExamException;
import indi.etern.checkIn.throwable.exam.ExamIllegalStateException;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserService extends DefaultOAuth2UserService implements UserDetailsService {
    public static UserService singletonInstance;
    private final OAuth2Service oauth2Service;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final WebSocketService webSocketService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private OAuth2BindingRepository oAuth2BindingRepository;
    private final ObjectMapper objectMapper;

    protected UserService(RoleService roleService, RoleRepository roleRepository, WebSocketService webSocketService, OAuth2Service oauth2Service, ObjectMapper objectMapper) {
        singletonInstance = this;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.webSocketService = webSocketService;
        this.oauth2Service = oauth2Service;
        this.objectMapper = objectMapper;
    }

    public boolean check(long qq, String password) {
        if (qq <= 0 || password == null || password.isEmpty()) {
            return false;
        }
        final Optional<User> optionalUser = findByQQNumber(qq);
        return optionalUser.isPresent() && optionalUser.get().getPassword().equals(password);
    }

    public void save(User user) {
        roleService.save(user.getRole());
    }

    public List<User> findAllByName(String name) {
        return userRepository.findAllByName(name);
    }

    public Optional<User> findByQQNumber(long qqNumber) {
        return userRepository.findById(qqNumber);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String qq) throws UsernameNotFoundException {
        try {
            final Optional<User> userOptional = findByQQNumber(Integer.parseInt(qq));
            if (userOptional.isPresent())
                return userOptional.get();
            else
                throw new UsernameNotFoundException("User not found:" + qq);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found:" + qq);
        }
    }

    public boolean existsByQQNumber(long qqNumber) {
        return userRepository.existsById(qqNumber);
    }

    public void saveAndFlush(User user) {
        userRepository.saveAndFlush(user);
    }

    public Set<User> findAllByRoleType(String roleType) {
        return roleRepository.findById(roleType).orElseThrow().getUsers();
    }

    public List<User> findAllByQQNumber(List<Long> qqNumbers) {
        return userRepository.findAllById(qqNumbers);
    }

    public void deleteAllByQQ(List<Long> qqNumbers) {
        userRepository.deleteAllById(qqNumbers);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void handleSignUp(ExamData examData, String name, String rawPassword, Role role, boolean enabled) throws ExamException {
        if (examData.getStatus() == ExamData.Status.ONGOING) {
            throw new ExamIllegalStateException();
        } else if (examData.getStatus() == ExamData.Status.SUBMITTED) {
            if (existsByQQNumber(examData.getQqNumber())) {
                throw new UserExistsException();
            } else {
                User user = new User(name, examData.getQqNumber(), rawPassword);
                user.setRole(role);
                user.setEnabled(enabled);
                save(user);
                for (Map.Entry<String, String> oAuth2BindingEntry : examData.getOAuth2Bindings().entrySet()) {
                    String id = oAuth2BindingEntry.getKey();
                    if (!oAuth2BindingRepository.existsByProviderId(id)) {
                        OAuth2Binding oAuth2Binding = OAuth2Binding.of(id, oAuth2BindingEntry.getValue());
                        oAuth2Binding.setUser(user);
                        oAuth2BindingRepository.save(oAuth2Binding);
                        user.getOauth2Bindings().add(oAuth2Binding);
                    }
                }
                save(user);
                Message<User> message = Message.of("addUser", user);
                webSocketService.sendMessageToAll(message);
            }
        } else {
            throw new ExamInvalidException();
        }
    }

    @SneakyThrows
    @Override
    @Transactional
    public User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Object subObject = oAuth2User.getAttributes().get(getSubKey(userRequest));
        String providerId = userRequest.getClientRegistration().getRegistrationId();
        User user = null;
        if (subObject != null) {
            Optional<User> optionalUser = findByOAuth2ProviderAndUserId(providerId, subObject.toString());
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }
        } else {
            throw new OAuth2AuthenticationException("Subject not found in OAuth2 attributes");
        }

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletResponse response = servletRequestAttributes.getResponse();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (response != null) {
                Cookie[] cookies = request.getCookies();
                String oAuth2Mode = null;
                String previousToken = null;
                String previousExamToken = null;
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        switch (cookie.getName()) {
                            case "token" -> previousToken = cookie.getValue();
                            case "OAuth2Mode" -> oAuth2Mode = cookie.getValue();
                            case "examToken" -> previousExamToken = cookie.getValue();
                        }
                    }
                }
                switch (oAuth2Mode) {
                    case "binding" -> {
                        if (user != null) {
                            throw new OAuth2AuthenticationException("该第三方账号已绑定其他账号");
                        } else {
                            user = JwtTokenProvider.singletonInstance.getUser(previousToken);
                            OAuth2Binding oAuth2Binding = OAuth2Binding.of(providerId, subObject.toString());
                            oAuth2Binding.setUser(user);
                            oAuth2BindingRepository.save(oAuth2Binding);
                            user.getOauth2Bindings().add(oAuth2Binding);
                            save(user);
                            return user;
                        }
                    }
                    case "login" -> {
                        if (user != null) {
                            addCookies(user, response);
                        } else {
                            throw new OAuth2AuthenticationException("找不到对应用户");
                        }
                    }
                    case "exam" -> {
                        User anonymous = User.ANONYMOUS;
                        Jws<Claims> claimsJws;
                        if (previousExamToken != null) {
                            claimsJws = JwtTokenProvider.singletonInstance.parseToken(previousExamToken);
                        } else {
                            claimsJws = null;
                        }
                        String examToken = JwtTokenProvider.singletonInstance.generateToken(anonymous, jwtBuilder -> {
                            Map<String, String> oAuth2Map;
                            if (claimsJws != null && claimsJws.getHeader().get("OAuth2") instanceof Map<?,?> map) {
                                //noinspection unchecked
                                oAuth2Map = (Map<String, String>) map;
                            } else {
                                oAuth2Map = new HashMap<>();
                            }
                            oAuth2Map.put(providerId, subObject.toString());
                            jwtBuilder.header().add("OAuth2", oAuth2Map);
                        });
                        Cookie examTokenCookie = new Cookie("examToken", examToken);
                        examTokenCookie.setPath("/checkIn");
                        response.addCookie(examTokenCookie);
                        return anonymous;
                    }
                    case null, default -> throw new OAuth2AuthenticationException("不支持的验证类型");
                }
            }
        }
        return user;
    }

    private void addCookies(User user, HttpServletResponse response) throws JsonProcessingException {
        String token = JwtTokenProvider.singletonInstance.generateToken(user);
        Map<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("name", user.getName());
        userDataMap.put("qq", user.getQQNumber());
        userDataMap.put("role", user.getRole().getType());
        userDataMap.put("token", token);
        String userData = URLEncoder.encode(objectMapper.writeValueAsString(userDataMap), StandardCharsets.UTF_8)
                .replace("+", "%20");
        Cookie userCookie = new Cookie("user", userData);
        Cookie tokenCookie = new Cookie("token", token);
        userCookie.setPath("/checkIn");
        tokenCookie.setPath("/checkIn");
        response.addCookie(userCookie);
        response.addCookie(tokenCookie);
    }

    public Optional<User> findByOAuth2ProviderAndUserId(String providerId, String userId) {
        Optional<OAuth2Binding> optionalBinding = oAuth2BindingRepository.findByProviderIdAndUserId(providerId, userId);
        return optionalBinding.map(OAuth2Binding::getUser);
    }

    private String getSubKey(OAuth2UserRequest userRequest) {//TODO
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ProviderInfo oAuth2ProviderInfo = oauth2Service.getProviderInfo(provider);
        return oAuth2ProviderInfo.getUserIdAttributeName();
    }
}