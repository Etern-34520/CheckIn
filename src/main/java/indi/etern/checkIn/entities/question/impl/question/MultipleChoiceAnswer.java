package indi.etern.checkIn.entities.question.impl.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.utils.QuestionIdSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class MultipleChoiceAnswer extends Answer<MultipleChoicesQuestion, List<String>> implements SingleQuestionAnswer {
    @Setter(AccessLevel.PROTECTED)
    private Set<Choice> selectedChoices;
    @Setter(AccessLevel.PROTECTED)
    private CheckedResult result;
    @JsonSerialize(using = QuestionIdSerializer.class)
    private MultipleChoicesQuestion source;
    
    @Override
    protected void initFromSource(MultipleChoicesQuestion multipleChoicesQuestion, List<String> source) {
        this.source = multipleChoicesQuestion;
        selectedChoices = multipleChoicesQuestion.choices.stream().filter(choice -> source.contains(choice.getId())).collect(Collectors.toSet());
    }
    
    @Override
    public CheckedResult check() {
        SettingItem scoreSettingItem = SettingService.singletonInstance.findItem("grading.questionScore").orElseThrow();
        float maxScore = scoreSettingItem.getValue(Number.class).floatValue();
        if (result == null) {
            final boolean correct = source.checkAnswer(this);
            if (correct) {
                result = new CheckedResult(maxScore, maxScore, CheckedResultType.CORRECT);
            } else {
                result = new CheckedResult(0, maxScore, CheckedResultType.WRONG);//TODO half correct supports
            }
        }
        return result;
    }
}
