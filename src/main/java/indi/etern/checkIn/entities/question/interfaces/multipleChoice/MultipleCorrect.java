package indi.etern.checkIn.entities.question.interfaces.multipleChoice;

import java.util.List;

public interface MultipleCorrect extends MultipleChoice{
    List<Choice> getCorrectChoices();
    boolean checkAnswers(List<Choice> choices);
}
