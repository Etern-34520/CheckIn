package indi.etern.checkIn.entities.question.impl;

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
        if (source.isEmpty()) {
            selectedChoices = Set.of();
        } else {
            selectedChoices = multipleChoicesQuestion.choices.stream().filter(choice -> source.contains(choice.getId())).collect(Collectors.toSet());
        }
    }
    
    record RateData(float rate, Answer.CheckedResultType checkedResultType) {}
    
    @Override
    public CheckedResult check() {
        if (result == null) {
            final SettingItem item = SettingService.singletonInstance.getItem("grading", "multipleChoicesQuestionsCheckingStrategy");
            final SettingItem item1 = SettingService.singletonInstance.getItem("grading", "enableLosePoints");
            boolean enableLosePoints = item1.getValue(Boolean.class);
            SettingItem scoreSettingItem = SettingService.singletonInstance.getItem("generating","questionScore");
            float maxScore = scoreSettingItem.getValue(Number.class).floatValue();
            
            final MultipleChoicesQuestion.AnswerCheckingStrategy answerCheckingStrategy = MultipleChoicesQuestion.AnswerCheckingStrategy.valueOf(item.getValue(String.class).toUpperCase());
            final RateData rateData = answerCheckingStrategy.checkAnswer(source, this);
            float rate = rateData.rate;
            if (!enableLosePoints && rate < 0) {
                rate = 0f;
            }
            result = new CheckedResult(maxScore * rate, maxScore, rateData.checkedResultType);
        }
        return result;
    }
}
