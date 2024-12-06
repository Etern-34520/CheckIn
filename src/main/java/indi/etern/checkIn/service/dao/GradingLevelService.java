package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.repositories.GradingLevelRepository;
import org.springframework.stereotype.Service;

@Service
public class GradingLevelService {
    private final GradingLevelRepository gradingLevelRepository;
    public static GradingLevelService singletonInstance;
    protected GradingLevelService(GradingLevelRepository gradingLevelRepository) {
        singletonInstance = this;
        this.gradingLevelRepository = gradingLevelRepository;
    }
    
    public void saveAll(Iterable<GradingLevel> gradingLevels) {
        gradingLevelRepository.saveAll(gradingLevels);
    }
    
    public void save(GradingLevel gradingLevel) {
        gradingLevelRepository.save(gradingLevel);
    }
    
    public void deleteAll() {
        gradingLevelRepository.deleteAll();
    }
    
    public Iterable<GradingLevel> findAll() {
        return gradingLevelRepository.findAll();
    }
    
    public GradingLevel findById(String id) {
        return gradingLevelRepository.findById(id).orElseThrow();
    }
    
    public void delete(GradingLevel gradingLevel) {
        gradingLevelRepository.delete(gradingLevel);
    }
    
    public void deleteById(String id) {
        gradingLevelRepository.deleteById(id);
    }
    
    public boolean existsById(String id) {
        return gradingLevelRepository.existsById(id);
    }
}