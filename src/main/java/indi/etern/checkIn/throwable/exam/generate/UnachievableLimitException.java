package indi.etern.checkIn.throwable.exam.generate;

public class UnachievableLimitException extends ExamGenerateFailedException {
    public UnachievableLimitException(Exception e) {
        super(e);
    }
    
    public UnachievableLimitException() {
        super();
    }
    
    @Override
    public String getDescription() {
        return "无法在当前选项下生成试题";
    }
}
