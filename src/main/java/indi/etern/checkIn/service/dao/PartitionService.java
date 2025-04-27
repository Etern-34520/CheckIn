package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    
    @CachePut(value = "partition",key = "#partition.id")
    public Partition save(Partition partition) {
        return partitionRepository.save(partition);
    }

    public List<Partition> findAll() {
        return partitionRepository.findAll();
    }
    
    public Optional<Partition> findByName(String name) {
        return partitionRepository.findByName(name);
    }
    
    @CacheEvict(value = "partition",key = "#partition.id")
    public void delete(Partition partition) {
        partitionRepository.delete(partition);
    }

    @Cacheable(value = "partition",key = "#id")
    public Optional<Partition> findById(String id) {
        return partitionRepository.findById(id);
    }

    public List<Partition> findAllByIds(Collection<String> partitionId) {
        return partitionRepository.findAllById(partitionId);
    }

    public boolean existsByName(String partitionName) {
        return partitionRepository.existsByName(partitionName);
    }
}