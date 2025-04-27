package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.utils.QuestionCreateUtils;
import jakarta.annotation.Nonnull;

import java.util.Map;
import java.util.Optional;

@Action(value = "createOrUpdateQuestion", exposed = false)
public class CreateOrUpdateQuestion extends BaseAction<CreateOrUpdateQuestion.Input, CreateOrUpdateQuestion.SuccessOutput> {
    public record Input(@Nonnull Map<String,Object> questionDataMap) implements InputData {}
    public record SuccessOutput(Question question) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    final QuestionService questionService;
    
    public CreateOrUpdateQuestion(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        final Input input = context.getInput();
        final Map<String, Object> questionDataMap = input.questionDataMap;
        Optional<Question> previousQuestion = questionService.findById(questionDataMap.get("id").toString());
        Question question = QuestionCreateUtils.createMultipleChoicesQuestion(questionDataMap);
        if (previousQuestion.isPresent() && questionDataMap.containsKey("authorQQ")) {
            context.requirePermission("change question author");
        }
        if (previousQuestion.isEmpty() ||
                previousQuestion.get().getAuthor() != null &&
                context.isCurrentUser(previousQuestion.get().getAuthor())) {
            context.requirePermission("create and edit owns questions");
        } else {
            context.requirePermission("edit others questions");
        }
        if (questionDataMap.containsKey("enabled")) {
            context.requirePermission("enable and disable questions");
        }
        questionService.save(question);
        context.resolve(new SuccessOutput(question));
    }
}