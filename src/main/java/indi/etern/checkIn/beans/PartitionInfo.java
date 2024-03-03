package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PartitionInfo {
    private final PartitionService partitionService;
    private final MultiPartitionableQuestionService multiPartitionableQuestionService;
    //Bean占位
    private List<Partition> partitions;
    //Bean占位
    private final List<String> partitionName = new ArrayList<>();
    private List<Partition> partitionNotEmpty;
    private Partition getByName;
    private Long questionCount;
    
    public PartitionInfo(PartitionService partitionService, MultiPartitionableQuestionService multiPartitionableQuestionService) {
        this.partitionService = partitionService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }
    
    public List<Partition> getPartitions() {
        partitions = partitionService.findAll();
        return partitions;
    }
    
    public List<Partition> getPartitionsNotEmpty() {
        getPartitions();
        partitionNotEmpty = new ArrayList<>();
        for (Partition partition : partitions) {
            if (!partition.getQuestions().isEmpty()) {
                partitionNotEmpty.add(partition);
            }
        }
        return partitionNotEmpty;
    }
    
    public List<String> getPartitionName() {
        getPartitions();
        partitionName.clear();
        for (Partition partition : partitions) {
            partitionName.add(partition.getName());
        }
        return partitionName;
    }
    
    public long getQuestionCount() {
        return multiPartitionableQuestionService.countEnabled();
    }
    
}
