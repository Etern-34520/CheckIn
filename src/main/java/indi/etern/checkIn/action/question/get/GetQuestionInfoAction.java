package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.dto.manage.question.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.question.ManageDTOUtils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action("getQuestionInfo")
public class GetQuestionInfoAction extends BaseAction<GetQuestionInfoAction.Input,OutputData> {
    private final VerificationRuleService verificationRuleService;
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

            verificationRuleService.updateValidation(commonQuestionDTO, question);
        }
    }
}