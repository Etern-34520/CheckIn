package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.LinkedHashMap;
import java.util.Map;

@Action("getExamRecordDetail")
public class GetExamRecordDetailAction extends BaseAction1<GetExamRecordDetailAction.Input, OutputData> {
    public record Input(String id) implements InputData {
    }
    
    public record SuccessOutput(Map<String, Object> exanData) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final ExamDataService examDataService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public GetExamRecordDetailAction(ExamDataService examDataService, JwtTokenProvider jwtTokenProvider) {
        this.examDataService = examDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        examDataService.findById(context.getInput().id)
                .ifPresentOrElse((examData) -> {
                    final User currentUser = context.getCurrentUser();
                    final boolean isCurrentUserAccess = examData.getQqNumber() != currentUser.getQQNumber();
                    if (isCurrentUserAccess) {
                        context.requirePermission("get exam data");
                    }
                    final boolean accessibleToOthersSubmissions = context.hasPermission("get exam submission data");
                    final LinkedHashMap<String, Object> examDataMap = examData.toDataMap();
                    if (isCurrentUserAccess && !accessibleToOthersSubmissions) {
                        examDataMap.remove("answers");
                    }
                    context.resolve(new SuccessOutput(examDataMap));
                }, () -> {
                    context.resolve(MessageOutput.error("Exam data not found"));
                });
    }
}
