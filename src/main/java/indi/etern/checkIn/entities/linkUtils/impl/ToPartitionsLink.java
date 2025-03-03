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
    
    @ManyToMany(cascade = {CascadeType.REFRESH,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "questions_link_mapping",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "partition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)))
    Set<Partition> targets = new HashSet<>();
    
    {
        linkType = LinkType.PARTITION_LINK;
    }
    
    @FunctionalInterface
    public interface Configurator {
        void configure(ToPartitionsLink linkWrapper);
    }
}