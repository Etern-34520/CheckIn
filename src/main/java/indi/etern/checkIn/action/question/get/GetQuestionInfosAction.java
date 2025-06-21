package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.*;
import indi.etern.checkIn.dto.manage.CommonQuestionDTO;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Action("getQuestionInfos")
public class GetQuestionInfosAction extends BaseAction<GetQuestionInfosAction.Input, OutputData> {
    private final ActionExecutor actionExecutor;
    
    public GetQuestionInfosAction(ActionExecutor actionExecutor) {
        super();
        this.actionExecutor = actionExecutor;
    }
    
    public record Input(@Nonnull List<String> questionIds) implements InputData { }
    
    public record Output(Result result,
                         List<CommonQuestionDTO> questions,
                         List<String> missingIds) implements OutputData { }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        final List<String> questionIds = input.questionIds;
        final List<CommonQuestionDTO> dataList = new ArrayList<>(input.questionIds.size());
        final List<String> missingIds = new ArrayList<>(0);
        for (String questionId : questionIds) {
            ResultContext<OutputData> context1 = actionExecutor.execute(GetQuestionInfoAction.class, new GetQuestionInfoAction.Input(questionId));
            final OutputData output = context1.getOutput();
            if (output instanceof GetQuestionInfoAction.SuccessOutput(CommonQuestionDTO commonQuestionDTO)
                    && output.result().equals(OutputData.Result.SUCCESS)) {
                dataList.add(commonQuestionDTO);
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
