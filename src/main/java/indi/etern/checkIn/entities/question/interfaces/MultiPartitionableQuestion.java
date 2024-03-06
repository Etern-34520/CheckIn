package indi.etern.checkIn.entities.question.interfaces;

import indi.etern.checkIn.entities.question.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public class MultiPartitionableQuestion extends Question implements MultiPartitionable {
    
    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "partitions_questions_mapping",
            joinColumns  = @JoinColumn(name = "question_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "partition_id",referencedColumnName = "id"))
    protected Set<Partition> partitions;

    @Getter
    @Setter
    protected boolean enabled = false;
    
    @Override
    public Set<Partition> getPartitions() {
        return partitions;
    }
    
    @Override
    public Set<Integer> getPartitionIds() {
        Set<Integer> ids = new HashSet<>();
        for (Partition partition : partitions) {
            ids.add(partition.getId());
        }
        return ids;
    }
    
    @Override
    public String toJsonData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

}
