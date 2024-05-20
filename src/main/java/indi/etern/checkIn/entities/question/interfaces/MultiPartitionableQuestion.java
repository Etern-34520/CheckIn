package indi.etern.checkIn.entities.question.interfaces;

import indi.etern.checkIn.entities.question.Question;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public class MultiPartitionableQuestion extends Question implements MultiPartitionable {
    
    @ManyToMany(cascade = {CascadeType.REFRESH,CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "partitions_questions_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "partition_id", referencedColumnName = "id"))
    protected Set<Partition> partitions;
    
    @Getter
    @Setter
    protected boolean enabled = false;
    
    @Getter
    @JoinTable(name = "upvoters_questions_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqNumber"))
    @ManyToMany(cascade = {
            CascadeType.REFRESH
    }, fetch = FetchType.EAGER)
    protected Set<User> upVoters = new HashSet<>();
    
    @Getter
    @ManyToMany(cascade = {
            CascadeType.REFRESH
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "downvoters_questions_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqNumber"))
    protected Set<User> downVoters = new HashSet<>();
    
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
}