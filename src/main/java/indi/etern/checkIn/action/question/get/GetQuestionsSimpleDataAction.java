package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Action("getQuestionSimpleData")
public class GetQuestionsSimpleDataAction extends BaseAction<GetQuestionsSimpleDataAction.Input, OutputData> {
    public record Input(@Nonnull String partitionId) implements InputData {}
    public record SuccessOutput(List<Map<String,Object>> questionList) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(input.partitionId);
        if (optionalPartition.isPresent()) {
            List<Map<String,Object>> questionList = new ArrayList<>();
            for (var questionLink : optionalPartition.get().getQuestionLinks()) {
                final LinkedHashMap<String, Object> questionInfo = getSimpleInfoMap(questionLink);
                questionList.add(questionInfo);
            }
            context.resolve(new SuccessOutput(questionList));
        } else {
            context.resolve(MessageOutput.error("Partition not found"));
        }
    }
    
    private static LinkedHashMap<String, Object> getSimpleInfoMap(ToPartitionsLink questionLink) {
        Question question = questionLink.getSource();
        LinkedHashMap<String, Object> questionInfo = new LinkedHashMap<>();
        questionInfo.put("id", question.getId());
        questionInfo.put("content", question.getContent());
        final User author = question.getAuthor();
        if (author != null)
            questionInfo.put("authorQQ", author.getQQNumber());
        questionInfo.put("type", question.getClass().getSimpleName());
        return questionInfo;
    }
}