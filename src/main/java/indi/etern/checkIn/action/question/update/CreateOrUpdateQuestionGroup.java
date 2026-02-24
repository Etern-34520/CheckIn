package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.dto.manage.question.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.question.IssueDTO;
import indi.etern.checkIn.dto.manage.question.QuestionGroupDTO;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import indi.etern.checkIn.service.exam.StatusService;
import indi.etern.checkIn.utils.QuestionCreateUtils;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Action(value = "createOrUpdateQuestionGroup",exposed = false)
public class CreateOrUpdateQuestionGroup extends BaseAction<CreateOrUpdateQuestionGroup.Input, OutputData> {
    private final VerificationRuleService verificationRuleService;
    
    public record Input(@Nonnull QuestionGroupDTO questionGroupDTO) implements InputData {}
    public record SuccessOutput(QuestionGroup questionGroup) implements OutputData {
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

    public CreateOrUpdateQuestionGroup(QuestionService questionService, VerificationRuleService verificationRuleService) {
        this.questionService = questionService;
        this.verificationRuleService = verificationRuleService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        final var questionGroupDTO = input.questionGroupDTO;
        Optional<Question> previousQuestion = questionService.findById(questionGroupDTO.getId());
        final boolean authorChanged = previousQuestion.isPresent() && questionGroupDTO.getAuthorQQ() != null;
        previousQuestion.ifPresent(questionGroupDTO::inheritFrom);
        final ValidationResult result = verificationRuleService.verify(questionGroupDTO, VerificationRuleService.VerifyTargetType.QUESTION_GROUP);
        final Map<String, IssueDTO> errors = result.getErrors();
        if (errors.isEmpty()) {
            final QuestionGroup questionGroup = QuestionCreateUtils.createQuestionGroup(questionGroupDTO);
            if (authorChanged) {
                context.requirePermission("change question group author");
            }
            if (previousQuestion.isEmpty() ||
                    previousQuestion.get().getAuthor() != null &&
                            context.isCurrentUser(previousQuestion.get().getAuthor())) {
                context.requirePermission("create and edit owns question groups");
            } else {
                context.requirePermission("edit others question groups");
            }
            Boolean dtoEnabled = questionGroupDTO.getEnabled();
            if (dtoEnabled != null &&
                    ((previousQuestion.isPresent() && previousQuestion.get().isEnabled() != dtoEnabled) ||
                            (previousQuestion.isEmpty() && dtoEnabled))
            ) {
                context.requirePermission("enable and disable question groups");
            }
            Boolean dtoUnsafeXss = questionGroupDTO.getUnsafeXss();
            if (dtoUnsafeXss != null &&
                    ((previousQuestion.isPresent() && previousQuestion.get().isUnsafeXss() != dtoUnsafeXss) ||
                            (previousQuestion.isEmpty() && dtoUnsafeXss))
            ) {
                context.requirePermission("enable and disable unsafe xss for question groups");
            }            int count = 0;
            for (CommonQuestionDTO commonQuestionDTO : questionGroupDTO.getQuestions()) {
                final ValidationResult result1 = verificationRuleService.verify(commonQuestionDTO, VerificationRuleService.VerifyTargetType.MULTIPLE_CHOICES_QUESTION);
                if (!result1.getErrors().isEmpty()) {
                    count++;
                    for (Map.Entry<String, IssueDTO> entry : result1.getErrors().entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue().getContent();
                        errors.put(count + "-" + key, new IssueDTO("第" + (count) + "道子题目：" +value));
                    }
                }
            }
            
            if (errors.isEmpty()) {
                questionGroup.setVerificationDigest(verificationRuleService.digest(questionGroupDTO));
                questionGroup.setValidationResult(result);
                questionService.saveAll(questionGroup.getQuestionLinks().stream().map(QuestionLinkImpl::getSource).toList());
                questionService.save(questionGroup);
                context.resolve(new SuccessOutput(questionGroup));
                StatusService.singletonInstance.flush();
            } else {
                context.resolve(new ErrorOutput(errors.values().stream().map(IssueDTO::getContent).toList()));
            }
        } else {
            context.resolve(new ErrorOutput(errors.values().stream().map(IssueDTO::getContent).toList()));
        }
    }
}