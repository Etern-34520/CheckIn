package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionInfo {
    private final QuestionService multiPartitionableQuestionService;

    public QuestionInfo(QuestionService multiPartitionableQuestionService) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }

    public List<Question> getNewQuestionInDays(int dayCount, int count) {
        return multiPartitionableQuestionService.findEditedInLastDays(dayCount, count);
    }
}
