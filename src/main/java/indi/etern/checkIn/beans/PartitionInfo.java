package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PartitionInfo {
    @Autowired
    private PartitionService partitionService;
    //Bean占位
    private List<Partition> partitions;
    //Bean占位
    private final List<String> partitionName = new ArrayList<>();
    private Partition getByName;
    
    public List<Partition> getPartitions() {
        partitions = partitionService.findAll();
        return partitions;
    }
    
    public List<String> getPartitionName() {
        getPartitions();
        partitionName.clear();
        for (Partition partition : partitions) {
            partitionName.add(partition.getName());
        }
        return partitionName;
    }
    
    public Partition getGetByName(String name) {
        return partitionService.findByName(name);
    }
}
