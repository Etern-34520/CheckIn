package indi.etern.checkIn.entities.question.interfaces;

import indi.etern.checkIn.entities.question.Question;
import jakarta.persistence.*;

import java.util.Set;
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public class MultiPartitionableQuestion extends Question implements MultiPartitionable {
    
    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinTable(name = "partitions_questions_mapping",
            joinColumns  = @JoinColumn(name = "question_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "partition_id",referencedColumnName = "id"))
    protected Set<Partition> partitions;
    /*
    @Id
    @Column(name = "id")
    protected String id;
    */
    @Override
    public Set<Partition> getPartitions() {
        return partitions;
    }
}
