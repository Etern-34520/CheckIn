package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Resource
    private UserRepository userRepository;
    
    public boolean check(long qq, String password) {
        if (qq <= 0 || password == null || password.isEmpty()) {
            return false;
        }
        final Optional<User> optionalUser = findByQQNumber(qq);
        return optionalUser.isPresent() && optionalUser.get().getPassword().equals(password);
    }
    
    public void save(User user) {
        userRepository.save(user);
    }
    
    public List<User> findAllByName(String name) {
        User exampleUser = User.exampleOfName(name);
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withIgnorePaths("password", "QQNumber", "role");
        Example<User> userExample = Example.of(exampleUser, exampleMatcher);
        return userRepository.findAll(userExample);
    }
    
    public Optional<User> findByQQNumber(long qqNumber) {
        return userRepository.findById(qqNumber);
    }
    
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
