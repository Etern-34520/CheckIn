package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.repositories.GradingLevelRepository;
import indi.etern.checkIn.service.exam.StatusService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "gradingLevel")
public class GradingLevelService {
    private final GradingLevelRepository gradingLevelRepository;
    public static GradingLevelService singletonInstance;
    
    protected GradingLevelService(GradingLevelRepository gradingLevelRepository) {
        singletonInstance = this;
        this.gradingLevelRepository = gradingLevelRepository;
    }
    
    @CacheEvict(allEntries = true)
    public void saveAll(Iterable<GradingLevel> gradingLevels) {
        StatusService.singletonInstance.flush();
        gradingLevelRepository.saveAll(gradingLevels);
    }
    
    @CachePut(key = "#gradingLevel.id")
    public GradingLevel save(GradingLevel gradingLevel) {
        StatusService.singletonInstance.flush();
        return gradingLevelRepository.save(gradingLevel);
    }
    
    @CacheEvict(allEntries = true)
    public void deleteAll() {
        StatusService.singletonInstance.flush();
        gradingLevelRepository.deleteAll();
    }
    
    public List<GradingLevel> findAll() {
        return gradingLevelRepository.findAll(Sort.by(Sort.Order.by("levelIndex")));
    }
    
    @Cacheable(key = "#id")
    public GradingLevel findById(String id) {
        return gradingLevelRepository.findById(id).orElseThrow();
    }
    
    @CacheEvict(key = "#gradingLevel.id")
    public void delete(GradingLevel gradingLevel) {
        StatusService.singletonInstance.flush();
        gradingLevelRepository.delete(gradingLevel);
    }
}