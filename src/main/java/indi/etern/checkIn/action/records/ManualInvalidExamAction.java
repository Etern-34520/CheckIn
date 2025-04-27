package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.Optional;

@Action("InvalidExam")
public class ManualInvalidExamAction extends BaseAction<ManualInvalidExamAction.Input, MessageOutput> {
    public record Input(String id) implements InputData {}
    ;
    private final ExamDataService examDataService;
    
    public ManualInvalidExamAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("manual invalid exam");
        Optional<ExamData> optionalExamData = examDataService.findById(context.getInput().id);
        optionalExamData.ifPresentOrElse((examData) -> {
            examData.setStatus(ExamData.Status.MANUAL_INVALIDED);
            examDataService.save(examData);
            examData.sendUpdateExamRecord();
            context.resolve(MessageOutput.success("ExamController data invalid"));
        },() -> {
            context.resolve(MessageOutput.error("ExamController data not found"));
        });
    }
}
