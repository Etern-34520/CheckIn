package indi.etern.checkIn.entities.question.interfaces.answer;

import lombok.Getter;

@Getter
public abstract class Answer<QuestionType,Source> {
    public enum CheckedResultType {
        CORRECT,HALF_CORRECT,WRONG
    }
    public record CheckedResult(double score,CheckedResultType checkedResultType) {}
    protected abstract void initFromSource(QuestionType question,Source source);
    public abstract CheckedResult check();
}
