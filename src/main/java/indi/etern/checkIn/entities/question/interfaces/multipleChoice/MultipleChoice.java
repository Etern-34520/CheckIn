package indi.etern.checkIn.entities.question.interfaces.multipleChoice;

import indi.etern.checkIn.entities.question.impl.Choice;

import java.util.List;

public interface MultipleChoice {
    List<Choice> getChoices();
}