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
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Action("getQuestionSimpleData")
public class GetQuestionsSimpleDataAction extends BaseAction<GetQuestionsSimpleDataAction.Input, OutputData> {
    private final PartitionService partitionService;
    private final VerificationRuleService verificationRuleService;
    private final TransactionTemplate transactionTemplate;
    
    public GetQuestionsSimpleDataAction(PartitionService partitionService, VerificationRuleService verificationRuleService, TransactionTemplate transactionTemplate) {
        super();
        this.partitionService = partitionService;
        this.verificationRuleService = verificationRuleService;
        this.transactionTemplate = transactionTemplate;
    }
    
    public record Input(@Nonnull String partitionId) implements InputData {}
    public record SuccessOutput(List<BasicQuestionDTO> questionList) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
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
                    final BasicQuestionDTO questionInfo = ManageDTOUtils.ofQuestionBasic(questionLink.getSource());
                    final CommonQuestionDTO questionInfoForVerify = ManageDTOUtils.ofQuestion(questionLink.getSource());
                    final ValidationResult result = verificationRuleService.verify(questionInfoForVerify, VerificationRuleService.VerifyTargetType.ANY);
                    if (!result.getErrors().isEmpty()) {
                        questionInfo.getErrors().putAll(result.getErrors());
                    }
                    if (!result.getWarnings().isEmpty()) {
                        questionInfo.getWarnings().putAll(result.getWarnings());
                    }
                    questionList.add(questionInfo);
                }
                context.resolve(new SuccessOutput(questionList));
            } else {
                context.resolve(MessageOutput.error("Partition not found"));
            }
        });
    }
}