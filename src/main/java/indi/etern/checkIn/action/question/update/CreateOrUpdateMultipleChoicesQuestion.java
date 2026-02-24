package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.dto.manage.question.IssueDTO;
import indi.etern.checkIn.dto.manage.question.MultipleChoicesQuestionDTO;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import indi.etern.checkIn.utils.QuestionCreateUtils;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Action(value = "createOrUpdateQuestion", exposed = false)
public class CreateOrUpdateMultipleChoicesQuestion extends BaseAction<CreateOrUpdateMultipleChoicesQuestion.Input, OutputData> {
    private final VerificationRuleService verificationRuleService;
    
    public record Input(@Nonnull MultipleChoicesQuestionDTO multipleChoicesQuestionDTO) implements InputData {}
    public record SuccessOutput(Question question) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    public record ErrorOutput(Collection<String> messages) implements OutputData {
        @Override
        public Result result() {
            return Result.ERROR;
        }
    }
    
    final QuestionService questionService;
    
    public CreateOrUpdateMultipleChoicesQuestion(QuestionService questionService, VerificationRuleService verificationRuleService) {
        this.questionService = questionService;
        this.verificationRuleService = verificationRuleService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        final var multipleChoicesQuestionDTO = input.multipleChoicesQuestionDTO;
        Optional<Question> previousQuestion = questionService.findById(multipleChoicesQuestionDTO.getId());
        final boolean authorChanged = previousQuestion.isPresent() && multipleChoicesQuestionDTO.getAuthorQQ() != null;
        previousQuestion.ifPresent(multipleChoicesQuestionDTO::inheritFrom);
        final ValidationResult result = verificationRuleService.verify(multipleChoicesQuestionDTO, VerificationRuleService.VerifyTargetType.MULTIPLE_CHOICES_QUESTION);
        final Map<String, IssueDTO> errors = result.getErrors();
        if (errors.isEmpty()) {
            Question question = QuestionCreateUtils.createMultipleChoicesQuestion(multipleChoicesQuestionDTO);
            if (authorChanged) {
                context.requirePermission("change question author");
            }
            if (previousQuestion.isEmpty() ||
                    previousQuestion.get().getAuthor() != null &&
                            context.isCurrentUser(previousQuestion.get().getAuthor())) {
                context.requirePermission("create and edit owns questions");
            } else {
                context.requirePermission("edit others questions");
            }
            Boolean dtoEnabled = multipleChoicesQuestionDTO.getEnabled();
            if (dtoEnabled != null &&
                    ((previousQuestion.isPresent() && previousQuestion.get().isEnabled() != dtoEnabled) ||
                            (previousQuestion.isEmpty() && dtoEnabled))
            ) {
                context.requirePermission("enable and disable questions");
            }
            Boolean dtoUnsafeXss = multipleChoicesQuestionDTO.getUnsafeXss();
            if (dtoUnsafeXss != null &&
                    ((previousQuestion.isPresent() && previousQuestion.get().isUnsafeXss() != dtoUnsafeXss) ||
                            (previousQuestion.isEmpty() && dtoUnsafeXss))
            ) {
                context.requirePermission("enable and disable unsafe xss for questions");
            }
            question.setVerificationDigest(verificationRuleService.digest(multipleChoicesQuestionDTO));
            question.setValidationResult(result);
            questionService.save(question);
            context.resolve(new SuccessOutput(question));
        } else {
            context.resolve(new CreateOrUpdateMultipleChoicesQuestion.ErrorOutput(errors.values().stream().map(IssueDTO::getContent).toList()));
        }
    }
}