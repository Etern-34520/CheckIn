package indi.etern.checkIn.action.question.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.dto.manage.question.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.question.MultipleChoicesQuestionDTO;
import indi.etern.checkIn.dto.manage.question.QuestionGroupDTO;
import indi.etern.checkIn.entities.question.impl.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Action("getQuestionInfos")
public class GetQuestionInfosAction extends BaseAction<GetQuestionInfosAction.Input, OutputData> {
    private final QuestionService questionService;

    public GetQuestionInfosAction(QuestionService questionService) {
        super();
        this.questionService = questionService;
    }

    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        final List<String> questionIds = input.questionIds;
        final Map<String, CommonQuestionDTO> dataMap = new HashMap<>();
        List<Question> questions = questionService.findAllById(questionIds);
        final List<String> succeedQuestionIds = new ArrayList<>();
        for (Question question : questions) {
            String questionId = question.getId();
            CommonQuestionDTO questionDTO;
            if (question instanceof MultipleChoicesQuestion multipleChoicesQuestion) {
                questionDTO = new MultipleChoicesQuestionDTO(multipleChoicesQuestion);
            } else if (question instanceof QuestionGroup questionGroup) {
                questionDTO = new QuestionGroupDTO(questionGroup);
            } else {
                throw new IllegalStateException("Unable to classify question");
            }
            succeedQuestionIds.add(questionId);
            dataMap.put(questionId, questionDTO);
        }
        questionIds.removeAll(succeedQuestionIds);
        int failureCount = questionIds.size();
        int successCount = succeedQuestionIds.size();

        if (failureCount == 0) {
            context.resolve(new Output(OutputData.Result.SUCCESS, dataMap));
        } else if (successCount != 0) {
            context.resolve(new Output(OutputData.Result.WARNING, dataMap));
        } else {
            context.resolve(new Output(OutputData.Result.ERROR, dataMap));
        }
    }

    public record Input(@Nonnull List<String> questionIds) implements InputData {
    }

    public record Output(
            Result result,
            @JsonInclude(JsonInclude.Include.ALWAYS) Map<String, CommonQuestionDTO> questions
    ) implements OutputData {
    }
}
