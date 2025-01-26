package indi.etern.checkIn.entities.question.impl.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class QuestionGroupAnswer extends Answer<QuestionGroup, List<SingleQuestionAnswer>> {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    List<SingleQuestionAnswer> answers;
    @JsonIgnore
    private QuestionGroup questionGroup;
    
    @Override
    protected void initFromSource(QuestionGroup questionGroup, List<SingleQuestionAnswer> answers) {
        this.questionGroup = questionGroup;
        this.answers = answers;
    }
    
    @Override
    public CheckedResult check() {
        SettingItem scoreSettingItem = SettingService.singletonInstance.findItem("grading.questionScore").orElseThrow();
        float singleMaxScore = scoreSettingItem.getValue(Number.class).floatValue();
        int maxCount = questionGroup.questionLinks.size();
        int correctCount = (int) answers.stream()
                .filter(answer ->
                        answer.check().checkedResultType() == CheckedResultType.CORRECT)
                .count();
        double score = correctCount * singleMaxScore;
        double maxScore = singleMaxScore * maxCount;
        CheckedResultType checkedResultType;
        if (maxScore == score) {
            checkedResultType = CheckedResultType.CORRECT;
        } else if (score < maxScore && score > 0) {
            checkedResultType = CheckedResultType.HALF_CORRECT;
        } else if (score == 0) {
            checkedResultType = CheckedResultType.WRONG;
        } else {
            throw new IllegalStateException();
        }
        return new CheckedResult(score, checkedResultType);
    }
}
