package indi.etern.checkIn.entities.linkUtils.impl;

import indi.etern.checkIn.entities.linkUtils.ToManyLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
//@Table("partitions_questions_mapping")
@Getter
public class ToPartitionLink extends QuestionLinkImpl<Partition> implements ToManyLink<Question, Partition> {
    /*@ManyToMany(mappedBy = "questions")*/
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "questions_link_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "partition_id", referencedColumnName = "id"))
    Set<Partition> targets = new HashSet<>();
    
    @FunctionalInterface
    public interface Configurator {
        void configure(ToPartitionLink linkWrapper);
    }
}