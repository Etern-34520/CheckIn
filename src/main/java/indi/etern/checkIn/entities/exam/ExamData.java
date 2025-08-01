package indi.etern.checkIn.entities.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.converter.MapConverter;
import indi.etern.checkIn.entities.converter.ObjectJsonConverter;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.entities.question.impl.QuestionGroupAnswer;
import indi.etern.checkIn.entities.question.impl.MultipleChoicesQuestion;
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
public class ExamData implements BaseEntity<String> , Comparable<ExamData>{
    
    @Override
    public int compareTo(@NonNull ExamData o) {
        if (this.examResult == null) {
            return -1;
        } else if (o.examResult == null) {
            return 1;
        } else {
            return (int) (this.examResult.getScore() - o.examResult.getScore());
        }
    }
    
    public enum Status {
        ONGOING, SUBMITTED, MANUAL_INVALIDED, EXPIRED, SIGN_UP_COMPLETED
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
    
    //    @Convert(converter = ListJsonConverter.class)
    @ElementCollection(targetClass = String.class)
    @CollectionTable(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    @Column(columnDefinition = "mediumtext")
    @JsonIgnore
    List<String> requiredPartitionIds;
    
    //    @Convert(converter = ListJsonConverter.class)
    @ElementCollection(targetClass = String.class)
    @CollectionTable(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    @Column(columnDefinition = "mediumtext")
    @JsonIgnore
    List<String> selectedPartitionIds;
    
    //    @Convert(converter = ListJsonConverter.class)
    @ElementCollection(targetClass = String.class)
    @CollectionTable(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    @Column(columnDefinition = "mediumtext")
    @JsonIgnore
    List<String> questionIds;
    
    @Convert(converter = MapConverter.class)
    @Column(name = "answers", columnDefinition = "mediumtext")
    @JsonIgnore
    Map<String, Answer<?, ?>> answersMap;
    
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
        int correctCount = 0;
        int halfCorrectCount = 0;
        int wrongCount = 0;
        float score = 0;
        for (Answer<?, ?> answer1 : answersMap.values()) {
            final Answer.CheckedResult checkedResult = answer1.check();
            switch (checkedResult.checkedResultType()) {
                case CORRECT -> correctCount++;
                case HALF_CORRECT -> halfCorrectCount++;
                case WRONG -> wrongCount++;
            }
            score += checkedResult.score();
        }
        examResult.setCorrectCount(correctCount);
        examResult.setHalfCorrectCount(halfCorrectCount);
        examResult.setWrongCount(wrongCount);
        examResult.setScore((float) Math.round(score * 10) / 10);
        return examResult;
    }
    
    private Optional<QuestionGroupAnswer> handleAnswerItemMap(List<Question> orderedQuestions, Map<String, Object> answer, QuestionGroup questionGroup) {
        List<SingleQuestionAnswer> questionGroupSubQuestionAnswers = new ArrayList<>();
        for (Map.Entry<String, Object> entry : answer.entrySet()) {
            int index = Integer.parseInt(entry.getKey());
            Question question = orderedQuestions.get(index);
            
            Object value = entry.getValue();
            if (question instanceof MultipleChoicesQuestion multipleChoicesQuestion) {
                List<?> choiceIndexes;
                if (value instanceof List<?> choiceIndexes1) {
                    choiceIndexes = choiceIndexes1;
                } else {
                    choiceIndexes = List.of();
                }
                //noinspection unchecked
                var choiceAnswer = multipleChoicesQuestion.newAnswerFrom((List<String>) choiceIndexes);
                if (questionGroup == null) {
                    this.answersMap.put(multipleChoicesQuestion.getId(), choiceAnswer);
                } else {
                    questionGroupSubQuestionAnswers.add(choiceAnswer);
                }
            } else if (value instanceof Map<?, ?> subQuestionAnswerMap && question instanceof QuestionGroup questionGroup1) {
                List<Question> subQuestions = questionGroup1.getQuestionLinks().stream().map(QuestionLinkImpl::getSource).toList();
                //noinspection unchecked
                this.answersMap.put(questionGroup1.getId(), handleAnswerItemMap(subQuestions,
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
        examDataMap.put("requiredPartitionIds", List.copyOf(requiredPartitionIds));
        examDataMap.put("selectedPartitionIds", List.copyOf(selectedPartitionIds));
        examDataMap.put("questionAmount", questionAmount);
        examDataMap.put("questionIds", List.copyOf(questionIds));
        examDataMap.put("result", examResult);
        examDataMap.put("answers", answersMap);
        return examDataMap;
    }
    
    public void sendUpdateExamRecord() {
        WebSocketService.singletonInstance.sendMessageToChannel(Message.of("updateExamRecord", this), "examRecords");
    }
}