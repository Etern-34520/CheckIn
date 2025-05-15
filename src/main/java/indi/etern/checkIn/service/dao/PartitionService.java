package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = "partition")
public class PartitionService {
    public static PartitionService singletonInstance;
    @Resource
    private PartitionRepository partitionRepository;
    private final Cache cache;

    protected PartitionService(CacheManager cacheManager) {
        singletonInstance = this;
        cache = cacheManager.getCache("partition");
    }
    
    @CachePut(key = "#partition.id")
    public Partition save(Partition partition) {
        return partitionRepository.save(partition);
    }

    public List<Partition> findAll() {
        return partitionRepository.findAll();
    }
    
    public Optional<Partition> findByName(String name) {
        return partitionRepository.findByName(name);
    }
    
    @CacheEvict(key = "#partition.id")
    public void delete(Partition partition) {
        partitionRepository.delete(partition);
    }

    @Cacheable(key = "#id")
    public Optional<Partition> findById(String id) {
        return partitionRepository.findById(id);
    }

    public List<Partition> findAllByIds(Collection<String> partitionIds) {
        List<String> uncachedPartitionIds = new ArrayList<>();
        List<Partition> partitions = new ArrayList<>(partitionIds.size());
        for (String partitionId : partitionIds) {
            final Partition partition = cache.get(partitionId, Partition.class);
            if (partition == null) {
                uncachedPartitionIds.add(partitionId);
            } else {
                partitions.add(partition);
            }
        }
        final List<Partition> allById = partitionRepository.findAllById(uncachedPartitionIds);
        for (Partition partition : allById) {
            cache.put(partition.getId(), partition);
        }
        partitions.addAll(allById);
        return partitions;
    }

    public boolean existsByName(String partitionName) {
        return partitionRepository.existsByName(partitionName);
    }
}