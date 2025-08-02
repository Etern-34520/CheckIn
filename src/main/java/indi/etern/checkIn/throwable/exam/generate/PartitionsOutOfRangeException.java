package indi.etern.checkIn.throwable.exam.generate;

public class PartitionsOutOfRangeException extends ExamGenerateFailedException {
    @Override
    public String getDescription() {
        return "提交的分区数量不在限制范围内";
    }
}
