package indi.etern.checkIn.dto.manage;

import indi.etern.checkIn.entities.question.impl.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;

public class ManageDTOUtils {
    public static CommonQuestionDTO ofQuestion(Question question){
        if (question instanceof MultipleChoicesQuestion multipleChoicesQuestion) {
            return new MultipleChoicesQuestionDTO(multipleChoicesQuestion);
        } else if (question instanceof QuestionGroup questionGroup) {
            return new QuestionGroupDTO(questionGroup);
        } else {
            return null;
        }
    }
    
    public static BasicQuestionDTO ofQuestionBasic(Question question){
        return new BasicQuestionDTO(question);
    }
}
