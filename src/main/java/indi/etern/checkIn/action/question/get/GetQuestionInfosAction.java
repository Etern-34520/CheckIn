package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.*;

@Action("getQuestionInfos")
public class GetQuestionInfosAction extends TransactionalAction {
    private final List<String> questionIds = new java.util.ArrayList<>();

    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        questionIds.addAll((List<String>) dataMap.get("questionIds"));
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        var questions = QuestionService.singletonInstance.findAllById(questionIds);
        LinkedHashMap<String,Object> result = getSuccessMap();
        ArrayList<Object> questionArray = new ArrayList<>(questions.size());
        for (Question question : questions) {
            questionIds.remove(question.getId());
            questionArray.add(QuestionUpdateUtils.getMapOfQuestion(question));
        }
        if (!questionIds.isEmpty()) {
            for (String questionId : questionIds) {
                LinkedHashMap<String,Object> notFound = getErrorMap("question not found");
                notFound.put("type", "question not found");
                notFound.put("id", questionId);
                questionArray.add(notFound);
            }
        }
        result.put("questions", questionArray);
        return Optional.of(result);
    }
}
