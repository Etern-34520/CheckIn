package indi.etern.checkIn.entities.question.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.utils.UUIDv7;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Embeddable
public class Choice implements Serializable {
    @Column(columnDefinition = "char(36)", unique = true, nullable = false)
    public String id;
    
    @Column(columnDefinition = "varchar(256)")
    private String content;
    
    @JsonIgnore
    private Boolean isCorrect;
    
    @Setter
    @JsonIgnore
    private int orderIndex;
    
    protected Choice() {
        this.id = UUIDv7.randomUUID().toString();
    }
    
    public Choice(String content, boolean isCorrect) {
        this.id = UUIDv7.randomUUID().toString();
        this.content = content;
        this.isCorrect = isCorrect;
    }
    
    @JsonIgnore
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