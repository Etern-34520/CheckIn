package indi.etern.checkIn.action.status;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.service.exam.StatusService;

@Action("getServiceStatuses")
public class GetServiceStatusesAction extends BaseAction<NullInput, GetServiceStatusesAction.SuccessOutput> {
    private final StatusService statusService;
    
    public GetServiceStatusesAction(StatusService statusService) {
        super();
        this.statusService = statusService;
    }
    
    public record SuccessOutput(StatusService.ServerStatuses serverStatuses) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        context.resolve(new SuccessOutput(statusService.getStatus()));
    }
}
