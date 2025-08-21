package indi.etern.checkIn.action.question.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.*;
import indi.etern.checkIn.dto.manage.CommonQuestionDTO;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Action("getQuestionInfos")
public class GetQuestionInfosAction extends BaseAction<GetQuestionInfosAction.Input, OutputData> {
    private final ActionExecutor actionExecutor;

    public GetQuestionInfosAction(ActionExecutor actionExecutor) {
        super();
        this.actionExecutor = actionExecutor;
    }

    public record Input(@Nonnull List<String> questionIds) implements InputData { }

    public record Output(
            Result result,
            @JsonInclude(JsonInclude.Include.ALWAYS) Map<String, CommonQuestionDTO> questions
    ) implements OutputData { }

    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        final List<String> questionIds = input.questionIds;
        final Map<String, CommonQuestionDTO> dataMap = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        for (String questionId : questionIds) {
            ResultContext<OutputData> context1 = actionExecutor.execute(GetQuestionInfoAction.class, new GetQuestionInfoAction.Input(questionId));
            final OutputData output = context1.getOutput();
            if (output instanceof GetQuestionInfoAction.SuccessOutput(CommonQuestionDTO commonQuestionDTO)
                    && output.result().equals(OutputData.Result.SUCCESS)) {
                dataMap.put(commonQuestionDTO.getId(), commonQuestionDTO);
                successCount++;
            } else {
                dataMap.put(questionId, null);
                failureCount++;
            }
        }
        if (failureCount == 0) {
            context.resolve(new Output(OutputData.Result.SUCCESS, dataMap));
        } else if (successCount != 0) {
            context.resolve(new Output(OutputData.Result.WARNING, dataMap));
        } else {
            context.resolve(new Output(OutputData.Result.ERROR, dataMap));
        }
    }
}
