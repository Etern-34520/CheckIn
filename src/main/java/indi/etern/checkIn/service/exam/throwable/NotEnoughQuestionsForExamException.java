package indi.etern.checkIn.service.exam.throwable;

public class NotEnoughQuestionsForExamException extends Exception {
    public NotEnoughQuestionsForExamException(String s) {
        super(s);
    }
    public NotEnoughQuestionsForExamException() {
        super();
    }
    
    public NotEnoughQuestionsForExamException(RuntimeException e) {
        super(e);
    }
}
