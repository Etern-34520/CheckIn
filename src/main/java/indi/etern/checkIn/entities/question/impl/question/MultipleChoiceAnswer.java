package indi.etern.checkIn.entities.question.impl.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;
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
    @JsonIgnore
    private MultipleChoicesQuestion multipleChoicesQuestion;
    
    @Override
    protected void initFromSource(MultipleChoicesQuestion multipleChoicesQuestion, List<String> source) {
        this.multipleChoicesQuestion = multipleChoicesQuestion;
        selectedChoices = multipleChoicesQuestion.choices.stream().filter(choice -> source.contains(choice.getId())).collect(Collectors.toSet());
    }
    
    @Override
    public CheckedResult check() {
        SettingItem scoreSettingItem = SettingService.singletonInstance.findItem("grading.questionScore").orElseThrow();
        float maxScore = scoreSettingItem.getValue(Number.class).floatValue();
        if (result == null) {
            final boolean correct = multipleChoicesQuestion.checkAnswer(this);
            if (correct) {
                result = new CheckedResult(maxScore, CheckedResultType.CORRECT);
            } else {
                result = new CheckedResult(0, CheckedResultType.WRONG);//TODO half correct supports
            }
        }
        return result;
    }
}
