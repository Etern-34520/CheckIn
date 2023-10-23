package indi.etern.checkIn.entities.question.interfaces.multipleChoice;

import indi.etern.checkIn.entities.question.impl.multipleQuestion.SingleCorrectQuestion;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import jakarta.persistence.*;

import java.io.Serializable;
@Entity
@Table(name = "choices")
public class Choice implements Serializable {
    @Id
    private String content;
    private Boolean isCorrect;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
    
    @OneToOne(mappedBy = "correctChoice", optional = false)
    private SingleCorrectQuestion singleCorrectQuestion;
    
    public SingleCorrectQuestion getSingleCorrectQuestion() {
        return singleCorrectQuestion;
    }
    
    public void setSingleCorrectQuestion(SingleCorrectQuestion singleCorrectQuestion) {
        this.singleCorrectQuestion = singleCorrectQuestion;
    }
}
