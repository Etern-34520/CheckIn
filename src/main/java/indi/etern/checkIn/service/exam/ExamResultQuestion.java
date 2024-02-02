package indi.etern.checkIn.service.exam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleCorrectQuestion;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.SingleCorrectQuestion;
import indi.etern.checkIn.service.exam.throwable.ExamException;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExamResultQuestion {
    private String id;
    private Object answer;
    String answerId;
    List<String> answerIds;

    protected ExamResultQuestion() {
    }
    
    private void convertAnswer() {
        if (answer instanceof String answerId1) {
            answerId = answerId1;
            answerIds = null;
        } else if (answer instanceof List<?> answerIds1) {
            answerId = null;
            //noinspection unchecked
            answerIds = (List<String>) answerIds1;
        } else {
            throw new ExamException("Unknown answer type");
        }
    }
    
    public boolean isMultiple() {
        if (answerId == null && answerIds == null)
            convertAnswer();
        return answerIds != null;
    }
    
    public boolean checkWith(MultipleChoiceQuestion multipleChoiceQuestion) {
        if (answerId == null && answerIds == null)
            convertAnswer();
        if (multipleChoiceQuestion instanceof MultipleCorrectQuestion mcq) {
            if (isMultiple()) {
                final HashSet<String> correctIds = new HashSet<>(mcq.getCorrectChoiceIds());
                final HashSet<String> answerIds1;
                if (answerIds != null) {
                    answerIds1 = new HashSet<>(answerIds);
                    return correctIds.containsAll(answerIds1) && answerIds1.containsAll(correctIds);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (multipleChoiceQuestion instanceof SingleCorrectQuestion scq) {
            if (isMultiple()) {
                return false;
            } else {
                return scq.getCorrectChoice().getId().equals(answerId);
            }
        } else {
            throw new ExamException("Unknown question type");
        }
    }
}
