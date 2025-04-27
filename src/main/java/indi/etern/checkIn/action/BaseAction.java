package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;

public abstract class BaseAction<I extends InputData,O extends OutputData>{
    public abstract void execute(ExecuteContext<I,O> context);
}