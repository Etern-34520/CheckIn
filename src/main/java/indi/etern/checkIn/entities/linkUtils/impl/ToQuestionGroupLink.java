package indi.etern.checkIn.entities.linkUtils.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.linkUtils.ToOneLink;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class ToQuestionGroupLink extends QuestionLinkImpl<QuestionGroup> implements ToOneLink<Question, QuestionGroup> {

    public void setTarget(QuestionGroup target) {
        this.target = target;
        target.addQuestionLink(this);
    }

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    protected QuestionGroup target;

    @Setter
    @Column(columnDefinition = "int")
    int orderIndex;

    @FunctionalInterface
    public interface Configurator {
        void configure(ToQuestionGroupLink linkWrapper);
    }
}