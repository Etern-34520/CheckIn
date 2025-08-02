package indi.etern.checkIn.throwable.exam.generate;

public class NotEnoughQuestionsForExamException extends ExamGenerateFailedException {
    public NotEnoughQuestionsForExamException(Exception e) {
        super(e);
    }
    
    public NotEnoughQuestionsForExamException() {
        super();
    }
    
    @Override
    public String getDescription() {
        return "可用题目不足";
    }
}
