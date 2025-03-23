package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;

public abstract class BaseAction1<I extends InputData,O extends OutputData>{
    public <C extends ExecuteContext<I, O>> void call(C context) {
        execute(context);
    }
    
    public abstract void execute(ExecuteContext<I,O> context);
}