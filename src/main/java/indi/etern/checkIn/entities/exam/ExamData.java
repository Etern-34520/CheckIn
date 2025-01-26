package indi.etern.checkIn.entities.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.convertor.ListJsonConverter;
import indi.etern.checkIn.entities.convertor.ObjectJsonConverter;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroupAnswer;
import indi.etern.checkIn.entities.question.impl.question.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.exam.ExamResult;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exam_data")
public class ExamData implements BaseEntity<String> {
    public enum Status {
        ONGOING, SUBMITTED, MANUAL_INVALIDED, EXPIRED
    }
    
    @Id
    @Column(columnDefinition = "char(36)")
    String id;
    long qqNumber;
    int questionAmount;
    
    @JsonIgnore
    @Setter
    Status status;
    
    @Convert(converter = ListJsonConverter.class)
    List<Integer> requiredPartitionIds;
    
    @Convert(converter = ListJsonConverter.class)
    List<Integer> selectedPartitionIds;
    
    @Convert(converter = ListJsonConverter.class)
    List<String> questionIds;
    
    @Convert(converter = ListJsonConverter.class)
    @Column(columnDefinition = "mediumtext")
    List<Answer<?,?>> answers;
    
    @Setter
    @Convert(converter = ObjectJsonConverter.class)
    @Column(columnDefinition = "mediumtext")
    ExamResult examResult;
    
    public ExamResult checkAnswerMap(Map<String, Object> answerMap) {
        List<Question> orderedQuestions =
                QuestionService.singletonInstance.findAllById(questionIds).stream()
                        .sorted((q1, q2) ->
                                questionIds.indexOf(q1.getId()) - questionIds.indexOf(q2.getId())
                        ).toList();
        answers = new ArrayList<>();
        handleAnswerItemMap(orderedQuestions, answerMap, null);
        final ExamResult examResult = ExamResult.from(this);
        examResult.setCorrectCount((int) answers.stream().filter(answer1 -> answer1.check().checkedResultType() == Answer.CheckedResultType.CORRECT).count());
        examResult.setHalfCorrectCount((int) answers.stream().filter(answer1 -> answer1.check().checkedResultType() == Answer.CheckedResultType.HALF_CORRECT).count());
        examResult.setWrongCount((int) answers.stream().filter(answer1 -> answer1.check().checkedResultType() == Answer.CheckedResultType.WRONG).count());
        examResult.setScore((float) answers.stream().mapToDouble(answer -> answer.check().score()).sum());
        return examResult;
    }
    
    private Optional<QuestionGroupAnswer> handleAnswerItemMap(List<Question> orderedQuestions, Map<String, Object> answer, QuestionGroup questionGroup) {
        List<SingleQuestionAnswer> questionGroupSubQuestionAnswers = new ArrayList<>();
        for (Map.Entry<String, Object> entry : answer.entrySet()) {
            int index = Integer.parseInt(entry.getKey());
            Question question = orderedQuestions.get(index);
            
            Object value = entry.getValue();
            if (value instanceof List<?> choiceIds && question instanceof MultipleChoicesQuestion multipleChoicesQuestion) {
                //noinspection unchecked
                var choiceAnswer = multipleChoicesQuestion.newAnswerFrom((List<String>) choiceIds);
                if (questionGroup == null) {
                    this.answers.add(choiceAnswer);
                } else {
                    questionGroupSubQuestionAnswers.add(choiceAnswer);
                }
            } else if (value instanceof Map<?, ?> subQuestionAnswerMap && question instanceof QuestionGroup questionGroup1) {
                List<Question> subQuestions = questionGroup1.getQuestionLinks().stream().map(QuestionLinkImpl::getSource).toList();
                //noinspection unchecked
                this.answers.add(handleAnswerItemMap(subQuestions,
                        (Map<String, Object>) subQuestionAnswerMap,
                        questionGroup1)
                        .orElseThrow(IllegalStateException::new));
            }
        }
        if (questionGroup != null) {
            final QuestionGroupAnswer questionGroupAnswer = questionGroup.newAnswerFrom(questionGroupSubQuestionAnswers);
            return Optional.of(questionGroupAnswer);
        } else {
            return Optional.empty();
        }
    }
}