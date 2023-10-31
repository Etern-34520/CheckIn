package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public Partition findByName(String name) {
        Partition examplePartition = Partition.getExample(name);
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withIgnorePaths("id");
        Example<Partition> example = Example.of(examplePartition, exampleMatcher);
        return partitionRepository.findOne(example).orElseThrow();//FIXME 懒加载报错
    }
    
    public void deleteAll() {
        partitionRepository.deleteAll();
    }
}
