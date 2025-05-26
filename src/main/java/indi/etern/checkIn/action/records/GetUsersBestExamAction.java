package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;

@Action("getUsersLatestExamRecord")
public class GetUsersBestExamAction extends BaseAction<NullInput, OutputData> {
    public record SuccessOutput(LinkedHashMap<String, Object> examData) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    private final ExamDataService examDataService;
    
    public GetUsersBestExamAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<NullInput,OutputData> context) {
        final User currentUser = context.getCurrentUser();
        examDataService.findAllByQQ(currentUser.getQQNumber()).stream()
                .max(Comparator.comparing(examData -> examData))
                .ifPresentOrElse((examData) -> {
                    final LinkedHashMap<String, Object> examDataMap = examData.toDataMap();
                    examDataMap.remove("answers");
                    context.resolve(new SuccessOutput(examDataMap));
                }, () -> context.resolve(MessageOutput.error("ExamController record not found")));
    }
}
