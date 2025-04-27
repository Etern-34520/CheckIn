package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.RequestRecordService;

import java.util.*;

@Action("getRelatedRequestOfExamData")
public class GetRelatedRequestRecordsAction extends BaseAction<GetRelatedRequestRecordsAction.Input, GetRelatedRequestRecordsAction.SuccessOutput> {
    public record Input(String examDataId) implements InputData {}
    public record SuccessOutput(List<RequestRecord> requestRecords) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final RequestRecordService requestRecordService;
    public GetRelatedRequestRecordsAction(RequestRecordService requestRecordService) {
        this.requestRecordService = requestRecordService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        context.requirePermission("get request records");
        final String examDataId = context.getInput().examDataId;
        final List<RequestRecord> requestRecords = requestRecordService.findAllByExamDataId(examDataId);
        List<RequestRecord> orderedRequestRecords = requestRecords.stream().sorted(Comparator.comparing(RequestRecord::getTime).reversed()).toList();
        context.resolve(new SuccessOutput(orderedRequestRecords));
    }
}
