package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import indi.etern.checkIn.repositories.RobotTokenRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RobotTokenService {
    @Resource
    RobotTokenRepository robotTokenRepository;
    
    public RobotTokenItem save(RobotTokenItem robotTokenItem) {
        return robotTokenRepository.save(robotTokenItem);
    }
    
    public List<RobotTokenItem> findAll() {
        return robotTokenRepository.findAll();
    }
    
    public void delete(RobotTokenItem robotTokenItem) {
        robotTokenRepository.delete(robotTokenItem);
    }
    
    public void deleteAll() {
        robotTokenRepository.deleteAll();
    }
    
    public void saveAll(List<RobotTokenItem> robotTokenItems) {
        robotTokenRepository.saveAll(robotTokenItems);
    }
    
    public void deleteAllById(List<String> deletedRobotTokenList) {
        robotTokenRepository.deleteAllById(deletedRobotTokenList);
    }
    
    public boolean existByToken(String token) {
        return robotTokenRepository.existsByToken(token);
    }
}