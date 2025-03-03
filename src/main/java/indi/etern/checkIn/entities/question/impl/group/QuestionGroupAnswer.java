package indi.etern.checkIn.entities.question.impl.group;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.utils.QuestionIdSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class QuestionGroupAnswer extends Answer<QuestionGroup, List<SingleQuestionAnswer>> {
    @Setter(AccessLevel.PROTECTED)
    List<SingleQuestionAnswer> answers;
    @Setter(AccessLevel.PROTECTED)
    private CheckedResult result;
    @JsonSerialize(using = QuestionIdSerializer.class)
    private QuestionGroup source;
    
    @Override
    protected void initFromSource(QuestionGroup questionGroup, List<SingleQuestionAnswer> answers) {
        this.source = questionGroup;
        this.answers = answers;
    }
    
    @Override
    public CheckedResult check() {
        if (result == null) {
            SettingItem scoreSettingItem = SettingService.singletonInstance.getItem("generating","questionScore");
            float singleMaxScore = scoreSettingItem.getValue(Number.class).floatValue();
            int maxCount = source.questionLinks.size();
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
            result = new CheckedResult(score, maxScore, checkedResultType);
        }
        return result;
    }
}
