package indi.etern.checkIn.service.exam.throwable;

public class MinQuestionLimitOutOfBoundsException extends Exception {
    public MinQuestionLimitOutOfBoundsException() {
        super("The minimum number of questions is out of bounds.");
    }
}