package indi.etern.checkIn.entities.linkUtils.impl;

import indi.etern.checkIn.entities.linkUtils.ToOneLink;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class ToQuestionGroupLink extends QuestionLinkImpl<QuestionGroup> implements ToOneLink<Question, QuestionGroup> {

    public void setTarget(QuestionGroup target) {
        this.target = target;
        target.addQuestionLink(this);
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.EAGER)
    protected QuestionGroup target;

    @Setter
    int orderIndex;

    @FunctionalInterface
    public interface Configurator {
        void configure(ToQuestionGroupLink linkWrapper);
    }
}