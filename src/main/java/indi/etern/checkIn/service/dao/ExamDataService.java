package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.repositories.ExamDataRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExamDataService {
    @Resource
    ExamDataRepository examDataRepository;
    
    public void save(ExamData examData) {
        examDataRepository.save(examData);
    }
    
    public Optional<ExamData> findById(String id) {
        return examDataRepository.findById(id);
    }
}
