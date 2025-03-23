package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PartitionService {
    public static PartitionService singletonInstance;
    final
    TransactionTemplate transactionTemplate;
    @Resource
    private PartitionRepository partitionRepository;

    protected PartitionService(TransactionTemplate transactionTemplate) {
        singletonInstance = this;
        this.transactionTemplate = transactionTemplate;
    }

    public void save(Partition partition) {
        partitionRepository.save(partition);
    }

    public List<Partition> findAll() {
        return partitionRepository.findAll();
    }
    
    public void saveAndFlush(Partition partition) {
        partitionRepository.saveAndFlush(partition);
    }

    public Optional<Partition> findByName(String name) {
        return partitionRepository.findByName(name);
    }

    public void deleteAll() {
        partitionRepository.deleteAll();
    }

    public void deleteByName(String partitionName) {
        Optional<Partition> optionalPartition = findByName(partitionName);
        optionalPartition.ifPresent(partition -> partitionRepository.deleteById(partition.getId()));
    }

    public void delete(Partition partition) {
        partitionRepository.delete(partition);
    }

    public Optional<Partition> findById(String id) {
        return partitionRepository.findById(id);
    }

    public List<Partition> findAllByIds(Collection<String> partitionId) {
        return partitionRepository.findAllById(partitionId);
    }

    public boolean existsByName(String partitionName) {
        return partitionRepository.existsByName(partitionName);
    }
    
    public void saveAll(Collection<Partition> partitions) {
        partitionRepository.saveAll(partitions);
    }
    
    public void flush() {
        partitionRepository.flush();
    }
    
    public boolean exists(Partition partition) {
        return partitionRepository.existsById(partition.getId());
    }
}
