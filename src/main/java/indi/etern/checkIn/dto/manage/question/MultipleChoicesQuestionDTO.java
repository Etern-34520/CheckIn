package indi.etern.checkIn.dto.manage.question;

import com.fasterxml.jackson.annotation.JsonTypeName;
import indi.etern.checkIn.entities.question.impl.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.Question;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@JsonTypeName("MultipleChoicesQuestion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class MultipleChoicesQuestionDTO extends CommonQuestionDTO {
    private List<ChoiceDTO> choices = null;
    
    public MultipleChoicesQuestionDTO(MultipleChoicesQuestion question) {
        super(question);
        choices = question.getChoices().stream().map(ChoiceDTO::new).toList();
    }
    
    @Override
    public void inheritFrom(Question question) {
        if (question instanceof MultipleChoicesQuestion multipleChoicesQuestion) {
            super.inheritFrom(question);
            if (choices == null && multipleChoicesQuestion.getChoices() != null) {
                choices = multipleChoicesQuestion.getChoices().stream()
                        .map(ChoiceDTO::new).toList();
            }
        } else {
            throw new IllegalArgumentException("Question must be QuestionGroup");
        }
    }
}