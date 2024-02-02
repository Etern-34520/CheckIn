package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionInfo {
    private final MultiPartitionableQuestionService multiPartitionableQuestionService;

    public QuestionInfo(MultiPartitionableQuestionService multiPartitionableQuestionService) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }

    public List<MultiPartitionableQuestion> getNewQuestionInDays(int dayCount) {
        return multiPartitionableQuestionService.findEditedInLastDays(dayCount);
    }
}
