package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Action("getQuestionInfo")
public class GetQuestionInfoAction extends BaseAction<GetQuestionInfoAction.Input,OutputData> {
    public record Input(@Nonnull String questionId) implements InputData {}
    public record SuccessOutput(Map<String,Object> question) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final QuestionService questionService;
    
    public GetQuestionInfoAction(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        Optional<Question> questionOptional = questionService.findById(input.questionId);
        if (questionOptional.isEmpty()) {
            context.resolve(MessageOutput.error("Question not found"));
        } else {
            context.resolve(new SuccessOutput(QuestionUpdateUtils.getMapOfQuestion(questionOptional.get())));
        }
    }
}