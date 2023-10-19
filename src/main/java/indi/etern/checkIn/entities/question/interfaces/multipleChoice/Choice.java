package indi.etern.checkIn.entities.question.interfaces.multipleChoice;

import java.io.Serializable;

public class Choice implements Serializable {
    private String content;
    private Boolean isCorrect;
    protected Choice() {
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public Choice(String content, boolean isCorrect) {
        this.content = content;
        this.isCorrect = isCorrect;
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
