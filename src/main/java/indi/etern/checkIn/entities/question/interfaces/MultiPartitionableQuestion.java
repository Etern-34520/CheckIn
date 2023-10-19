package indi.etern.checkIn.entities.question.interfaces;

import indi.etern.checkIn.dao.ExternalPersistence;
import indi.etern.checkIn.entities.question.Question;

import java.util.Set;
public abstract class MultiPartitionableQuestion extends Question implements MultiPartitionable {
    @ExternalPersistence
    protected transient Set<Partition> partitions;
    /*
    @Override
    public void afterLoad() {
        Set<Partition> newPartitions = new HashSet<>();
        for (Partition partition : partitions) {
            newPartitions.add(Partition.getInstance(partition.toString()));
        }
        partitions = newPartitions;
    }
    @Override
    public void beforeSave(Dao dao){
        dao.saveAll(partitions);
    }*/
    
    @Override
    public Set<Partition> getPartitions() {
        return partitions;
    }
}
