package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartitionService {
    @Resource
    private PartitionRepository partitionRepository;
    
    public static PartitionService singletonInstance;
    
    protected PartitionService() {
        singletonInstance = this;
    }
    
    public void save(Partition partition) {
        partitionRepository.save(partition);
    }
    
    public List<Partition> findAll() {
        return partitionRepository.findAll();
    }
    
    public void saveAndFlush(Partition partition){
        partitionRepository.saveAndFlush(partition);
    }
//    @Transactional(readOnly = true)
    public Partition findByName(String name) {
        Partition examplePartition = Partition.getExample(name);
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withIgnorePaths("id");
        Example<Partition> example = Example.of(examplePartition, exampleMatcher);
        return partitionRepository.findOne(example).orElseThrow();
    }
    
    public Optional<Partition> tryFindByName(String name) {
        Partition examplePartition = Partition.getExample(name);
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withIgnorePaths("id");
        Example<Partition> example = Example.of(examplePartition, exampleMatcher);
        return partitionRepository.findOne(example);
    }
    
    public void deleteAll() {
        partitionRepository.deleteAll();
    }
    
    public void deleteByName(String partitionName) {
        Partition partition = findByName(partitionName);
        partitionRepository.deleteById(partition.getId());
    }
    
    public void delete(Partition partition) {
        partitionRepository.delete(partition);
    }
    
    public boolean existsById(int id) {
        return partitionRepository.existsById(id);
    }
    
    public Optional<Partition> findById(Integer id) {
        return partitionRepository.findById(id);
    }
}
