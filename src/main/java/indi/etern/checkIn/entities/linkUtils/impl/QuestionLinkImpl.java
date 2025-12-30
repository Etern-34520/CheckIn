package indi.etern.checkIn.entities.linkUtils.impl;

import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.linkUtils.Link;
import indi.etern.checkIn.entities.question.impl.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorOptions;

@Getter
@Setter
@Entity
@Table(name = "question_link")
@DiscriminatorOptions(force = true)
public abstract class QuestionLinkImpl<T extends BaseEntity<?>> implements Link<Question, T>, BaseEntity<String> {
    
    @OneToOne(mappedBy = "linkWrapper")
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    protected Question source;
    
    @Id
    @Column(columnDefinition = "char(36)")
    protected String id;
    
    public void setSource(Question source) {
        this.source = source;
        id = source.getId();
    }
}
