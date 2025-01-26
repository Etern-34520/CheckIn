package indi.etern.checkIn.throwable.exam.generate;

public class NotEnoughQuestionsForExamException extends ExamGenerateFailedException {
    public NotEnoughQuestionsForExamException(Exception e) {
        super(e);
    }
    
    public NotEnoughQuestionsForExamException() {
        super();
    }
    
    @Override
    public String getEnDescription() {
        return "Not enough questions for exam";
    }
    
    @Override
    public String getCnDescription() {
        return "可用题目不足";
    }
}
