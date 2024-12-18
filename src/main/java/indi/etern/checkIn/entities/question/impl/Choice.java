package indi.etern.checkIn.entities.question.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Entity
@Table(name = "choices")
public class Choice implements Serializable {
    @Id
    @Column(name = "ID")
    private String id;
    
    @Column(name = "CONTENT")
    private String content;
    
    @Column(name = "IS_CORRECT")
    private Boolean isCorrect;
    
    @Setter
    @Column(name = "ORDER_INDEX")
    private int orderIndex;
    
/*
    @Setter
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(value = "question_id",referencedColumnName = "id")
    private MultipleChoicesQuestion question;
*/
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

}
