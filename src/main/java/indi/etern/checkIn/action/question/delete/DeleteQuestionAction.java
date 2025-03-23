package indi.etern.checkIn.action.question.delete;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action(value = "deleteQuestion", exposed = false)
public class DeleteQuestionAction extends BaseAction1<DeleteQuestionAction.Input, OutputData> {
    public record Input(@Nonnull String questionId) implements InputData {}
    public record Output(Question deletedQuestion) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final QuestionService questionService;
    protected DeleteQuestionAction(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        Optional<Question> optionalQuestion = questionService.findById(input.questionId);
        
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            final User currentUser = context.getCurrentUser();
            if (currentUser.equals(question.getAuthor())) {
                if (question instanceof QuestionGroup)
                    context.requirePermission("delete owns question groups");
                else
                    context.requirePermission("delete owns questions");
            } else {
                if (question instanceof QuestionGroup)
                    context.requirePermission("delete others question groups");
                else
                    context.requirePermission("delete others questions");
            }
            questionService.delete(question);
            context.resolve(new Output(question));
        } else {
            context.resolve(MessageOutput.warning("Question not found"));
        }
    }
}