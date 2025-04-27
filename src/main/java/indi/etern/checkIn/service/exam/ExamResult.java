package indi.etern.checkIn.service.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.exam.ExamData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public final class ExamResult implements Comparable<ExamResult> {
    private long qq;
    private float score;
    private int correctCount;
    private int halfCorrectCount;
    private int wrongCount;
    private int questionCount;
    private boolean showCreatingAccountGuide;
    private String message;
    private String level;
    @JsonIgnore
    private ExamData examData;
    @JsonIgnore
    private List<String> questionIds;
    private String colorHex;
    
    private ExamResult() {
    }
    
    public static ExamResult from(ExamData examData) {
        ExamResult examResult = new ExamResult();
        examResult.qq = examData.getQqNumber();
        examResult.examData = examData;
        examResult.questionCount = examResult.getQuestionCount();
        examResult.questionIds = examResult.getQuestionIds();
        return examResult;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExamResult) obj;
        return this.qq == that.qq &&
                this.score == that.score &&
                this.correctCount == that.correctCount &&
                this.halfCorrectCount == that.halfCorrectCount &&
                this.wrongCount == that.wrongCount &&
                this.questionCount == that.questionCount &&
                this.showCreatingAccountGuide == that.showCreatingAccountGuide &&
                Objects.equals(this.level, that.level) &&
                Objects.equals(this.message, that.message);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(qq, score, correctCount, halfCorrectCount, wrongCount, questionCount, showCreatingAccountGuide, message, level);
    }
    
    @Override
    public int compareTo(ExamResult o) {
        return (int) (this.score - o.score);
    }
}