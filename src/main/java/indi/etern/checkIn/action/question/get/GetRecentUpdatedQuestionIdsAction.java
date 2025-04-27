package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.ArrayList;
import java.util.List;

@Action("getRecentUpdatedQuestionIds")
public class GetRecentUpdatedQuestionIdsAction extends BaseAction<NullInput,GetRecentUpdatedQuestionIdsAction.SuccessOutput> {
    public record SuccessOutput(List<String> questionIds) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final QuestionService questionService;
    
    public GetRecentUpdatedQuestionIdsAction(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        var questions = questionService.findLatestModifiedQuestions();
        List<String> questionIds = new ArrayList<>(questions.size());
        for (Question question : questions) {
            questionIds.add(question.getId());
        }
        context.resolve(new SuccessOutput(questionIds));
    }
}