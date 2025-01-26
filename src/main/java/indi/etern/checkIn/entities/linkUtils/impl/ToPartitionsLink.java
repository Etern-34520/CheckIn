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
public class ToPartitionsLink extends QuestionLinkImpl<Partition> implements ToManyLink<Question, Partition> {
    /*@ManyToMany(mappedBy = "questions")*/
    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
/*
    @JoinTable(name = "questions_link_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "partition_id", referencedColumnName = "id"))
*/
    @JoinTable(name = "questions_link_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "partition_id", referencedColumnName = "id"), foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    Set<Partition> targets = new HashSet<>();
    
    @FunctionalInterface
    public interface Configurator {
        void configure(ToPartitionsLink linkWrapper);
    }
}