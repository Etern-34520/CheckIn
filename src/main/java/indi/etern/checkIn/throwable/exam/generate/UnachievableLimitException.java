package indi.etern.checkIn.throwable.exam.generate;

public class UnachievableLimitException extends ExamGenerateFailedException {
    public UnachievableLimitException(Exception e) {
        super(e);
    }
    
    public UnachievableLimitException() {
        super();
    }
    
    @Override
    public String getEnDescription() {
        return "Cannot generate exam in current generating options";
    }
    
    @Override
    public String getCnDescription() {
        return "无法在当前选项下生成试题";
    }
}
