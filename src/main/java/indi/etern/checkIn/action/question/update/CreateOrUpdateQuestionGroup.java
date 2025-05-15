package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.utils.QuestionCreateUtils;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Action(value = "createOrUpdateQuestionGroup",exposed = false)
public class CreateOrUpdateQuestionGroup extends BaseAction<CreateOrUpdateQuestionGroup.Input, CreateOrUpdateQuestionGroup.SuccessOutput> {
    public record Input(@Nonnull Map<String,Object> questionDataMap) implements InputData {}
    public record SuccessOutput(QuestionGroup questionGroup) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    final QuestionService questionService;

    public CreateOrUpdateQuestionGroup(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        final Input input = context.getInput();
        final QuestionGroup questionGroup = QuestionCreateUtils.createQuestionGroup(input.questionDataMap);
        Optional<Question> previousQuestion = questionService.findById(questionGroup.getId());
        if (previousQuestion.isPresent() && input.questionDataMap.containsKey("authorQQ")) {
            context.requirePermission("change question group author");
        }
        if (previousQuestion.isEmpty() ||
                previousQuestion.get().getAuthor() != null &&
                        context.isCurrentUser(previousQuestion.get().getAuthor())) {
            context.requirePermission("create and edit owns question groups");
        } else {
            context.requirePermission("edit others question groups");
        }
        if (input.questionDataMap.containsKey("enabled")) {
            context.requirePermission("enable and disable question groups");
        }
        questionService.saveAll(questionGroup.getQuestionLinks().stream().map(QuestionLinkImpl::getSource).toList());
        questionService.save(questionGroup);
        context.resolve(new SuccessOutput(questionGroup));
    }
}