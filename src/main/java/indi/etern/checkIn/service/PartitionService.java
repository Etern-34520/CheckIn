package indi.etern.checkIn.service;

import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartitionService {
    @Resource
    private PartitionRepository partitionRepository;
    
    public void save(Partition partition) {
        partitionRepository.save(partition);
    }
    
    public List<Partition> findAll() {
        return partitionRepository.findAll();
    }
    
    public void saveAndFlush(Partition partition){
        partitionRepository.saveAndFlush(partition);
    }
    
}
