package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.RoleRepository;
import indi.etern.checkIn.repositories.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    public static UserService singletonInstance;
    @Resource
    private UserRepository userRepository;
    private final RoleService roleService;
    private final TransactionTemplate template;
    @Autowired
    private RoleRepository roleRepository;
    
    protected UserService(RoleService roleService, TransactionTemplate template) {
        singletonInstance = this;
        this.roleService = roleService;
        this.template = template;
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
//        userRepository.save(user);
    }
    
    public List<User> findAllByName(String name) {
        User exampleUser = User.exampleOfName(name);
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withIgnorePaths("password", "QQNumber", "role", "enabled");
        Example<User> userExample = Example.of(exampleUser, exampleMatcher);
        return userRepository.findAll(userExample);
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
    
    
    
    public void unbindAndDeleteById(long id) {
        template.execute(status -> {
            final Optional<User> optionalUser = findByQQNumber(id);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                final Role role = user.getRole();
                role.getUsers().remove(user);
                user.setRole(null);
                userRepository.deleteById(id);
                roleService.save(role);
            }
            return Boolean.TRUE;
        });
    }
    
    public void unbindAndDelete(User user) {
        unbindAndDeleteById(user.getQQNumber());
        userRepository.flush();
    }
    
    public void enableByQQNumber(long qq) {
        final Optional<User> optionalUser = findByQQNumber(qq);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(true);
            save(user);
        } else {
            throw new UsernameNotFoundException("User not found:"+qq);
        }
    }
    
    public void disableByQQNumber(long qq) {
        final Optional<User> optionalUser = findByQQNumber(qq);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(false);
            save(user);
        } else {
            throw new UsernameNotFoundException("User not found:"+qq);
        }
    }
    
    public User changeRoleById(long qq, String roleType) {
        final User[] user = new User[1];
        template.execute(status -> {
            Role role = roleService.findByType(roleType).orElseThrow();
            user[0] = userRepository.findById(qq).orElseThrow();
            user[0].setRole(role);//TODO
            userRepository.save(user[0]);
            return Boolean.TRUE;
        });
        return user[0];
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
}