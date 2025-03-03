package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getRecentUpdatedQuestionIds")
public class GetRecentUpdatedQuestionIdsAction extends TransactionalAction {
    private final QuestionService questionService;
    
    public GetRecentUpdatedQuestionIdsAction(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        var questions = questionService.findLatestModifiedQuestions();
        LinkedHashMap<String,Object> result = getSuccessMap();
        ArrayList<String> questionArray = new ArrayList<>(questions.size());
        for (Question question : questions) {
            questionArray.add(question.getId());
        }
        result.put("questionIds", questionArray);
        return Optional.of(result);
    }
}