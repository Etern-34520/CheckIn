package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import indi.etern.checkIn.repositories.RobotTokenRepository;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "robotToken")
public class RobotTokenService {
    @Resource
    RobotTokenRepository robotTokenRepository;
    
    @CachePut(key = "#robotTokenItem.id")
    public RobotTokenItem save(RobotTokenItem robotTokenItem) {
        return robotTokenRepository.save(robotTokenItem);
    }
    
    public List<RobotTokenItem> findAll() {
        return robotTokenRepository.findAll();
    }
    
    @CacheEvict(key = "#robotTokenItem.id")
    public void delete(RobotTokenItem robotTokenItem) {
        robotTokenRepository.delete(robotTokenItem);
    }
    
    @CacheEvict(allEntries = true)
    public void saveAll(List<RobotTokenItem> robotTokenItems) {
        robotTokenRepository.saveAll(robotTokenItems);
    }
    
    @CacheEvict(allEntries = true)
    public void deleteAllById(List<String> deletedRobotTokenList) {
        robotTokenRepository.deleteAllById(deletedRobotTokenList);
    }
    
    public boolean existByToken(String token) {
        return robotTokenRepository.existsByToken(token);
    }
}