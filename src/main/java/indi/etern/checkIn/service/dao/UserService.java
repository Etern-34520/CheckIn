package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.RoleRepository;
import indi.etern.checkIn.repositories.UserRepository;
import indi.etern.checkIn.service.web.WebSocketService;
import indi.etern.checkIn.throwable.entity.UserExistsException;
import indi.etern.checkIn.throwable.exam.ExamException;
import indi.etern.checkIn.throwable.exam.ExamIllegalStateException;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    public static UserService singletonInstance;
    @Resource
    private UserRepository userRepository;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final WebSocketService webSocketService;
    
    protected UserService(RoleService roleService, RoleRepository roleRepository, WebSocketService webSocketService) {
        singletonInstance = this;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.webSocketService = webSocketService;
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
                throw new UsernameNotFoundException("User not found:"+qq);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found:"+qq);
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
                Message<User> message = Message.of("addUser", user);
                webSocketService.sendMessageToAll(message);
                save(user);
            }
        } else {
            throw new ExamInvalidException();
        }
    }
}