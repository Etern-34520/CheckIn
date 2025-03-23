package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.List;

@Action("getExamRecordsByQQ")
public class GetExamRecordsByQqAction extends BaseAction1<GetExamRecordsByQqAction.Input, GetExamRecordsByQqAction.SuccessOutput>{
    public record Input(long qq) implements InputData {}
    public record SuccessOutput(List<ExamData> examRecords) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    private final ExamDataService examDataService;
    private long qq;
    
    public GetExamRecordsByQqAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        qq = context.getInput().qq;
        if (qq != context.getCurrentUser().getQQNumber()) {
            context.requirePermission("get exam data");
        }
        final SuccessOutput output
                = new SuccessOutput(examDataService.findAllByQQ(qq));
        context.resolve(output);
    }
}