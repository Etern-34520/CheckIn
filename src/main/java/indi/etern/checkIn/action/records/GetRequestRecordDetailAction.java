package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.service.dao.RequestRecordService;

import java.util.LinkedHashMap;

@Action("getRequestRecordDetail")
public class GetRequestRecordDetailAction extends BaseAction<GetRequestRecordDetailAction.Input,OutputData> {
    public record Input(String id) implements InputData {}
    public record SuccessOutput(LinkedHashMap<String, Object> requestRecord) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    private final RequestRecordService requestRecordService;
    
    public GetRequestRecordDetailAction(RequestRecordService requestRecordService) {
        this.requestRecordService = requestRecordService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        context.requirePermission("get request records");
        requestRecordService.findById(input.id).ifPresentOrElse((requestRecord) -> {
            context.resolve(new SuccessOutput(requestRecord.toDataMap()));
        }, () -> {
            context.resolve(MessageOutput.error("Request record not found"));
        });
    }
}
