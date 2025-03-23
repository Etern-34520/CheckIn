package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.OutputData;

public record BasicOutput(OutputData.Result result) implements OutputData {
    public static final BasicOutput SUCCESS = new BasicOutput(OutputData.Result.SUCCESS);
    public static final BasicOutput WARNING = new BasicOutput(Result.WARNING);
    public static final BasicOutput ERROR = new BasicOutput(Result.ERROR);
}
