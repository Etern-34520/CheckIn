package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getQuestionInfo")
public class GetQuestionInfoAction extends TransactionalAction {
    private String questionId;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(questionId);
        if (questionOptional.isEmpty()) {
            LinkedHashMap<String,Object> notFound = new LinkedHashMap<>();
            notFound.put("type", "question not found");
            return Optional.of(notFound);
        } else {
            return Optional.of(QuestionUpdateUtils.getMapOfQuestion(questionOptional.get()));
        }
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        questionId = (String) dataMap.get("questionId");
    }
}