package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.OutputData;

public record MessageOutput(Result result, String message) implements OutputData {
    public static MessageOutput success(String message) {
        return new MessageOutput(Result.SUCCESS, message);
    }
    public static MessageOutput warning(String message) {
        return new MessageOutput(Result.WARNING, message);
    }
    public static MessageOutput error(String message) {
        return new MessageOutput(Result.ERROR, message);
    }
}
