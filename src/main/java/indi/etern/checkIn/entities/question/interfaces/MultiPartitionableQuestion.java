package indi.etern.checkIn.entities.question.interfaces;

import indi.etern.checkIn.entities.question.Question;
import jakarta.persistence.*;

import java.util.Set;
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public abstract class MultiPartitionableQuestion extends Question implements MultiPartitionable {
    @ManyToMany(mappedBy = "questions")
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
