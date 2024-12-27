package indi.etern.checkIn.service.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.exam.ExamData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public final class ExamResult {
    private long qq;
    private int score;
    private int correctCount;
    private int questionCount;
    private String message;
    private String level;
    private ExamData examData;
    @JsonIgnore
    private List<String> questionIds;

    public ExamResult() {}

    public ExamResult(long qq, int score, int correctCount, int questionCount, String message, String level) {
        this.qq = qq;
        this.score = score;
        this.correctCount = correctCount;
        this.questionCount = questionCount;
        this.message = message;
        this.level = level;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExamResult) obj;
        return this.qq == that.qq &&
                this.score == that.score &&
                this.correctCount == that.correctCount &&
                this.questionCount == that.questionCount &&
                Objects.equals(this.level, that.level) &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qq, score, correctCount, questionCount, message, level);
    }
    
}