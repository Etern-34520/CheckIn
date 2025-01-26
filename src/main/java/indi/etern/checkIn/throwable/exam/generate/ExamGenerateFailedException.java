package indi.etern.checkIn.throwable.exam.generate;

import indi.etern.checkIn.throwable.exam.ExamRuntimeException;

public abstract class ExamGenerateFailedException extends ExamRuntimeException {
    public ExamGenerateFailedException(Exception e) {
        super(e);
    }
    public ExamGenerateFailedException() {
        super();
    }
    public abstract String getEnDescription();
    public abstract String getCnDescription();
}
