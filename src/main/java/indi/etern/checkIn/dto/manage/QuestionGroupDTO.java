package indi.etern.checkIn.dto.manage;

import com.fasterxml.jackson.annotation.JsonTypeName;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@JsonTypeName("QuestionGroup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionGroupDTO extends CommonQuestionDTO {
    List<CommonQuestionDTO> questions;
    
    public QuestionGroupDTO(QuestionGroup questionGroup) {
        super(questionGroup);
        questions = questionGroup.getQuestionLinks().stream()
                .map(link -> ManageDTOUtils.ofQuestion(link.getSource()))
                .toList();
    }
    
    @Override
    public void inheritFrom(Question question) {
        if (question instanceof QuestionGroup questionGroup) {
            super.inheritFrom(question);
            if (questions == null) {
                questions = questionGroup.getQuestionLinks().stream()
                        .map(link ->
                                ManageDTOUtils.ofQuestion(link.getSource())
                        ).toList();
            }
        } else {
            throw new IllegalArgumentException("Question must be QuestionGroup");
        }
    }
}