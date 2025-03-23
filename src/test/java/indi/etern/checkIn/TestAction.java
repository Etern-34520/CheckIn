package indi.etern.checkIn;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.User;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Action(value = "test",exposed = false)
public class TestAction extends BaseAction1<TestAction.Input,TestAction.Output> {
    public record Input (@Nonnull String echo,@Nullable String permissionName) implements InputData {}
    public record Output (Result result,String echo, User currentUser) implements OutputData {}
    
    @Override
    public void execute(ExecuteContext<Input, Output> context) {
        Input input = context.getInput();
        if (input.permissionName != null) {
            context.requirePermission(input.permissionName);
        }
        context.resolve(new Output(OutputData.Result.SUCCESS,input.echo,context.getCurrentUser()));
    }
}
