package indi.etern.checkIn.entities.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.converter.ListJsonConverter;
import indi.etern.checkIn.entities.converter.MapConverter;
import indi.etern.checkIn.entities.converter.ObjectJsonConverter;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroupAnswer;
import indi.etern.checkIn.entities.question.impl.question.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.service.dao.ExamDataService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.exam.ExamResult;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    
//    @JsonIgnore
    @Setter
    Status status;
    
    public Status getStatus() {
        if (status == Status.ONGOING && LocalDateTime.now().isAfter(expireTime)) {
            status = Status.EXPIRED;
            ExamDataService.singletonInstance.save(this);
            sendUpdateExamRecord();
        }
        return status;
    }
    
    @Convert(converter = ListJsonConverter.class)
    @Column(columnDefinition = "mediumtext")
    @JsonIgnore
    List<Integer> requiredPartitionIds;
    
    @Convert(converter = ListJsonConverter.class)
    @Column(columnDefinition = "mediumtext")
    @JsonIgnore
    List<Integer> selectedPartitionIds;
    
    @Convert(converter = ListJsonConverter.class)
    @Column(columnDefinition = "mediumtext")
    @JsonIgnore
    List<String> questionIds;
    
    @Convert(converter = MapConverter.class)
    @Column(name = "answers", columnDefinition = "mediumtext")
    @JsonIgnore
    Map<String,Answer<?,?>> answersMap;
    
    @Setter
    @Convert(converter = ObjectJsonConverter.class)
    @Column(columnDefinition = "mediumtext")
    ExamResult examResult;
    
    @Setter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime generateTime;
    
    @Setter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime submitTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime expireTime;
    
    public ExamResult checkAnswerMap(Map<String, Object> answerMap) {
        List<Question> orderedQuestions =
                QuestionService.singletonInstance.findAllById(questionIds).stream()
                        .sorted((q1, q2) ->
                                questionIds.indexOf(q1.getId()) - questionIds.indexOf(q2.getId())
                        ).toList();
        answersMap = new HashMap<>();
        handleAnswerItemMap(orderedQuestions, answerMap, null);
        final ExamResult examResult = ExamResult.from(this);
        examResult.setCorrectCount((int) answersMap.values().stream().filter(answer1 -> answer1.check().checkedResultType() == Answer.CheckedResultType.CORRECT).count());
        examResult.setHalfCorrectCount((int) answersMap.values().stream().filter(answer1 -> answer1.check().checkedResultType() == Answer.CheckedResultType.HALF_CORRECT).count());
        examResult.setWrongCount((int) answersMap.values().stream().filter(answer1 -> answer1.check().checkedResultType() == Answer.CheckedResultType.WRONG).count());
        examResult.setScore((float) answersMap.values().stream().mapToDouble(answer -> answer.check().score()).sum());
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
                    this.answersMap.put(multipleChoicesQuestion.getId(),choiceAnswer);
                } else {
                    questionGroupSubQuestionAnswers.add(choiceAnswer);
                }
            } else if (value instanceof Map<?, ?> subQuestionAnswerMap && question instanceof QuestionGroup questionGroup1) {
                List<Question> subQuestions = questionGroup1.getQuestionLinks().stream().map(QuestionLinkImpl::getSource).toList();
                //noinspection unchecked
                this.answersMap.put(questionGroup1.getId(),handleAnswerItemMap(subQuestions,
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
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExamData examData && examData.id.equals(this.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "ExamData[" + id + "]";
    }
    
    public LinkedHashMap<String, Object> toDataMap() {
        LinkedHashMap<String, Object> examDataMap = new LinkedHashMap<>();
        examDataMap.put("id", id);
        examDataMap.put("qqNumber", qqNumber);
        examDataMap.put("status", getStatus());
        examDataMap.put("generateTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(generateTime));
        examDataMap.put("expireTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(expireTime));
        if (submitTime != null)
            examDataMap.put("submitTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(submitTime));
        examDataMap.put("requiredPartitionIds", requiredPartitionIds);
        examDataMap.put("selectedPartitionIds", selectedPartitionIds);
        examDataMap.put("questionAmount", questionAmount);
        examDataMap.put("questionIds", questionIds);
        examDataMap.put("result", examResult);
        examDataMap.put("answers", answersMap);
        return examDataMap;
    }
    
    public void sendUpdateExamRecord() {
        Map<Object,Object> updateData = new HashMap<>();
        updateData.put("type","updateExamRecord");
        updateData.put("examRecord", this);
        WebSocketService.singletonInstance.sendMessageToChannel(updateData,"examRecord");
    }
}