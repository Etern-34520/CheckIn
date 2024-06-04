package indi.etern.checkIn.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.utils.Utils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action(name = "getQuestionInfos")
public class GetQuestionInfosAction extends TransactionalAction {
    private final List<String> questionIds = new ArrayList<>();

    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    public boolean shouldLogging() {
        return false;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        questionIds.addAll((List<String>) dataMap.get("questionIds"));
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        var questions = QuestionService.singletonInstance.findAllById(questionIds);
        JsonObject result = new JsonObject();
        JsonArray questionArray = new JsonArray(questions.size());
        for (Question question : questions) {
            questionIds.remove(question.getId());
            questionArray.add(Utils.getJsonObjectOf(question));
        }
        if (!questionIds.isEmpty()) {
            for (String questionId : questionIds) {
                JsonObject notFound = new JsonObject();
                notFound.addProperty("type", "question not found");
                notFound.addProperty("id", questionId);
                questionArray.add(notFound);
            }
        }
        result.add("questions", questionArray);
        return Optional.of(result);
    }
}
