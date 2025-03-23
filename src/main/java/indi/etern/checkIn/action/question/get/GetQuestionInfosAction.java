package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.interfaces.*;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Action("getQuestionInfos")
public class GetQuestionInfosAction extends BaseAction1<GetQuestionInfosAction.Input, OutputData> {
    private final ActionExecutor actionExecutor;
    
    public GetQuestionInfosAction(ActionExecutor actionExecutor) {
        super();
        this.actionExecutor = actionExecutor;
    }
    
    public record Input(@Nonnull List<String> questionIds) implements InputData { }
    
    public record Output(Result result,
                         List<Map<String, Object>> questions,
                         List<String> missingIds) implements OutputData { }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        final List<String> questionIds = input.questionIds;
        final List<Map<String, Object>> dataList = new ArrayList<>(input.questionIds.size());
        final List<String> missingIds = new ArrayList<>(0);
        for (String questionId : questionIds) {
            ResultContext<OutputData> context1 = actionExecutor.execute(GetQuestionInfoAction.class, new GetQuestionInfoAction.Input(questionId));
            final OutputData output = context1.getOutput();
            if (output instanceof GetQuestionInfoAction.SuccessOutput(Map<String, Object> questionData)
                    && output.result().equals(OutputData.Result.SUCCESS)) {
                dataList.add(questionData);
            } else {
                missingIds.add(questionId);
            }
        }
        if (missingIds.isEmpty()) {
            context.resolve(new Output(OutputData.Result.SUCCESS, dataList, null));
        } else if (!dataList.isEmpty()) {
            context.resolve(new Output(OutputData.Result.WARNING, dataList, missingIds));
        } else {
            context.resolve(new Output(OutputData.Result.ERROR, null, missingIds));
        }
    }
}
