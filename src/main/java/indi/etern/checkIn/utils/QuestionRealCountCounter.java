package indi.etern.checkIn.utils;

import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;

import java.util.Collection;

public class QuestionRealCountCounter {
    public static int count(Collection<Question> questionList) {
        return questionList.stream().mapToInt(question -> question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1).sum();
    }
}