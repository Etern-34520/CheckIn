package indi.etern.checkIn.entities.linkUtils.impl;

import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.linkUtils.Link;
import indi.etern.checkIn.entities.question.impl.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "question_link")
public class QuestionLinkImpl<T extends BaseEntity<?>> implements Link<Question, T>, BaseEntity<String> {
    public enum LinkType {
        PARTITION_LINK,QUESTION_GROUP_LINK
    }
    
    protected LinkType linkType;
    
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
