package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.repositories.GradingLevelRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradingLevelService {
    private final GradingLevelRepository gradingLevelRepository;
    public static GradingLevelService singletonInstance;
    protected GradingLevelService(GradingLevelRepository gradingLevelRepository) {
        singletonInstance = this;
        this.gradingLevelRepository = gradingLevelRepository;
    }
    
    @CacheEvict(value = "gradingLevel",allEntries = true)
    public void saveAll(Iterable<GradingLevel> gradingLevels) {
        gradingLevelRepository.saveAll(gradingLevels);
    }
    
    @CachePut(value = "gradingLevel",key = "#gradingLevel.id")
    public GradingLevel save(GradingLevel gradingLevel) {
        return gradingLevelRepository.save(gradingLevel);
    }
    
    @CacheEvict(value = "gradingLevel",allEntries = true)
    public void deleteAll() {
        gradingLevelRepository.deleteAll();
    }
    
    public List<GradingLevel> findAll() {
        return gradingLevelRepository.findAll();
    }
    
    @Cacheable(value = "gradingLevel",key = "#id")
    public GradingLevel findById(String id) {
        return gradingLevelRepository.findById(id).orElseThrow();
    }
    
    @CacheEvict(value = "gradingLevel",key = "#gradingLevel.id")
    public void delete(GradingLevel gradingLevel) {
        gradingLevelRepository.delete(gradingLevel);
    }
}