package indi.etern.checkIn.entities.question.interfaces;

import java.io.Serializable;
import java.util.Set;

public interface MultiPartitionable extends Serializable {
    Set<Partition> getPartitions();
    
    Set<Integer> getPartitionIds();
}
