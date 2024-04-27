package indi.etern.checkIn.entities.question.interfaces.multipleChoice;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Entity
@Table(name = "choices")
public class Choice implements Serializable {
    @Id
    @Column(name = "ID")
    private String id;
    
    @Getter
    @Column(name = "CONTENT")
    private String content;
    
    @Getter
    @Column(name = "IS_CORRECT")
    private Boolean isCorrect;
    
    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",referencedColumnName = "id")
    private MultiPartitionableQuestion question;
    protected Choice() {
    }
    
    public Choice(String content, boolean isCorrect) {
        this.content = content;
        this.isCorrect = isCorrect;
        id = String.valueOf(super.hashCode());
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    @Override
    public String toString() {
        return "correct:"+ isCorrect +"|" + content;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Choice choice) {
            return choice.content.equals(this.content) && choice.isCorrect == this.isCorrect;
        } else {
            return false;
        }
    }
    
    public String getId() {
        return id;
    }
}
