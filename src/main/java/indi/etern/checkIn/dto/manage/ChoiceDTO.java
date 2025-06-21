package indi.etern.checkIn.dto.manage;

import indi.etern.checkIn.entities.question.impl.Choice;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceDTO {
    public String id;
    private String content;
    private boolean correct;
    private int orderIndex;
    public ChoiceDTO(Choice choice) {
        this.id = choice.getId();
        this.content = choice.getContent();
        this.correct = choice.getIsCorrect();
        this.orderIndex = choice.getOrderIndex();
    }
    
    public Choice toChoice() {
        final Choice choice = new Choice(content, correct);
        choice.setOrderIndex(orderIndex);
        return choice;
    }
}
