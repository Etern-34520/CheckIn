package indi.etern.checkIn.throwable.exam;

public class ExamRuntimeException extends RuntimeException{
    public ExamRuntimeException(Exception e) {
        super(e);
    }
    
    public ExamRuntimeException() {
        super();
    }
    
    public ExamRuntimeException(String s) {
        super(s);
    }
}
