package indi.etern.checkIn.entities.linkUtils.impl;

import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.linkUtils.Link;
import indi.etern.checkIn.entities.question.impl.Question;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "question_link")
public class QuestionLinkImpl<T extends BaseEntity<?>> implements Link<Question, T>, BaseEntity<String> {
    @OneToOne(mappedBy = "linkWrapper")
    protected Question source;
    @Id
    protected String id;
    
    public void setSource(Question source) {
        this.source = source;
        id = source.getId();
    }
}
