package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.dto.manage.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.ManageDTOUtils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action("getQuestionInfo")
public class GetQuestionInfoAction extends BaseAction<GetQuestionInfoAction.Input,OutputData> {
    private final VerificationRuleService verificationRuleService;
    private final Logger logger = LoggerFactory.getLogger(GetQuestionInfoAction.class);
    
    public record Input(@Nonnull String questionId) implements InputData {}
    public record SuccessOutput(CommonQuestionDTO question) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final QuestionService questionService;
    
    public GetQuestionInfoAction(QuestionService questionService, VerificationRuleService verificationRuleService) {
        this.questionService = questionService;
        this.verificationRuleService = verificationRuleService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        Optional<Question> questionOptional = questionService.findById(input.questionId);
        if (questionOptional.isEmpty()) {
            context.resolve(MessageOutput.error("Question not found"));
        } else {
            final Question question = questionOptional.get();
            final CommonQuestionDTO commonQuestionDTO = ManageDTOUtils.ofQuestion(question);
            context.resolve(new SuccessOutput(commonQuestionDTO));
            
            final String currentDigest = verificationRuleService.digest(commonQuestionDTO);
            final String previousDigest = question.getVerificationDigest();
            if (!currentDigest.equals(previousDigest)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Verify question due to invalid digest, previous:\"{}\", current:\"{}\"",previousDigest,currentDigest);
                }
                final ValidationResult result = verificationRuleService.verify(commonQuestionDTO, VerificationRuleService.VerifyTargetType.ANY);
                // 检查结果
                if (!result.getErrors().isEmpty()) {
                    logger.debug("Verify result errors:");
                    result.getErrors().forEach((key, msg) -> logger.debug("{}: {}", key, msg));
                    logger.debug("========");
                }
                if (!result.getWarnings().isEmpty()) {
                    logger.debug("Verify result warnings:");
                    result.getWarnings().forEach((key, msg) -> logger.debug("{}: {}", key, msg));
                    logger.debug("========");
                }
                question.setVerificationDigest(currentDigest);
                question.setValidationResult(result);
                questionService.save(question);
            }
        }
    }
}