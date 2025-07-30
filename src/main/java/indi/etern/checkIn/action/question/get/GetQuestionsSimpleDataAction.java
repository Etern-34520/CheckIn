package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.dto.manage.BasicQuestionDTO;
import indi.etern.checkIn.dto.manage.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.ManageDTOUtils;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Action("getQuestionSimpleData")
public class GetQuestionsSimpleDataAction extends BaseAction<GetQuestionsSimpleDataAction.Input, OutputData> {
    private final PartitionService partitionService;
    private final VerificationRuleService verificationRuleService;
    private final TransactionTemplate transactionTemplate;
    private final Logger logger = LoggerFactory.getLogger(GetQuestionsSimpleDataAction.class);
    
    public GetQuestionsSimpleDataAction(PartitionService partitionService, VerificationRuleService verificationRuleService, TransactionTemplate transactionTemplate) {
        super();
        this.partitionService = partitionService;
        this.verificationRuleService = verificationRuleService;
        this.transactionTemplate = transactionTemplate;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        transactionTemplate.executeWithoutResult(status -> {
            final Input input = context.getInput();
            Optional<Partition> optionalPartition = partitionService.findById(input.partitionId);
            if (optionalPartition.isPresent()) {
                List<BasicQuestionDTO> questionList = new ArrayList<>();
                for (var questionLink : optionalPartition.get().getQuestionLinks()) {
                    final Question question = questionLink.getSource();
                    final BasicQuestionDTO questionInfo = ManageDTOUtils.ofQuestionBasic(question);
                    final CommonQuestionDTO questionInfoForVerify = ManageDTOUtils.ofQuestion(question);
                    
                    ValidationResult result;
                    final String currentDigest = verificationRuleService.digest(questionInfoForVerify);
                    final String previousDigest = question.getVerificationDigest();
                    if (!currentDigest.equals(previousDigest)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Verify question[\"{}\"] due to invalid digest, previous:\"{}\", current:\"{}\"",question.getId(),previousDigest,currentDigest);
                        }
                        result = verificationRuleService.verify(questionInfoForVerify, VerificationRuleService.VerifyTargetType.ANY);
                        // 检查结果
                        if (logger.isDebugEnabled() && !result.getErrors().isEmpty()) {
                            logger.debug("Verify result errors:");
                            result.getErrors().forEach((key, msg) -> logger.debug("{}: {}", key, msg));
                            logger.debug("========");
                        }
                        if (logger.isDebugEnabled() && !result.getWarnings().isEmpty()) {
                            logger.debug("Verify result warnings:");
                            result.getWarnings().forEach((key, msg) -> logger.debug("{}: {}", key, msg));
                            logger.debug("========");
                        }
                        question.setVerificationDigest(currentDigest);
                        question.setValidationResult(result);
                    } else {
                        result = question.getValidationResult();
                    }
                    
                    if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                        questionInfo.getErrors().putAll(result.getErrors());
                    }
                    if (result.getWarnings() != null && !result.getWarnings().isEmpty()) {
                        questionInfo.getWarnings().putAll(result.getWarnings());
                    }
                    questionInfo.setShowError(result.isShowError());
                    questionInfo.setShowWarning(result.isShowWarning());
                    questionList.add(questionInfo);
                }
                context.resolve(new SuccessOutput(questionList));
            } else {
                context.resolve(MessageOutput.error("Partition not found"));
            }
        });
    }
    
    public record Input(@Nonnull String partitionId) implements InputData {
    }
    
    public record SuccessOutput(List<BasicQuestionDTO> questionList) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
}