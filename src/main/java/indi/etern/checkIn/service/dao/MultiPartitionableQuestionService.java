package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.repositories.MultiplePartitionableQuestionRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MultiPartitionableQuestionService {
    @Resource
    private MultiplePartitionableQuestionRepository multiplePartitionableQuestionRepository;
    
    public void save(MultiPartitionableQuestion multiPartitionableQuestion) {
        multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
    }
    
    public List<MultiPartitionableQuestion> findAll() {
        return multiplePartitionableQuestionRepository.findAll();
    }
    
    public void saveAndFlush(MultiPartitionableQuestion multiPartitionableQuestion){
        multiplePartitionableQuestionRepository.saveAndFlush(multiPartitionableQuestion);
    }
    
    public void deleteAll() {
        multiplePartitionableQuestionRepository.deleteAll();
    }
}

