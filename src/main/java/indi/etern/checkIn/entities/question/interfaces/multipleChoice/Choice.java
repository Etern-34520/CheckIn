package indi.etern.checkIn.entities.question.interfaces.multipleChoice;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import jakarta.persistence.*;

import java.io.Serializable;
@Entity
@Table(name = "choices")
public class Choice implements Serializable {
    @Id
    @Column(name = "ID")
    /*
    * don't know how it must be String
    * else jpa will throw exception
    */
    private String id;
    @Column(name = "CONTENT")
    private String content;
    @Column(name = "IS_CORRECT")
    private Boolean isCorrect;
    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH,CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",referencedColumnName = "id")
    private MultiPartitionableQuestion question;
    protected Choice() {
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public Choice(String content, boolean isCorrect) {
        this.content = content;
        this.isCorrect = isCorrect;
        id = String.valueOf(super.hashCode());
    }
    public void setQuestion(MultiPartitionableQuestion question){
        this.question = question;
    }
    public MultiPartitionableQuestion getQuestion(){
        return question;
    }
    public String getContent() {
        return content;
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
    
}
