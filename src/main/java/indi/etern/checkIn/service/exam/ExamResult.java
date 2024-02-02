package indi.etern.checkIn.service.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private boolean passed;
    @JsonIgnore
    private List<String> questionIds;

    protected ExamResult() {}

    public ExamResult(long qq, int score, int correctCount, int questionCount, String message, boolean passed) {
        this.qq = qq;
        this.score = score;
        this.correctCount = correctCount;
        this.questionCount = questionCount;
        this.message = message;
        this.passed = passed;
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
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qq, score, correctCount, questionCount, message);
    }

    @Override
    public String toString() {
        return "ExamResult[" +
                "qq=" + qq + ", " +
                "score=" + score + ", " +
                "correctCount=" + correctCount + ", " +
                "questionCount=" + questionCount + ", " +
                "message=" + message + ']';
    }

}
