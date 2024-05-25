package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.api.webSocket.action.question.utils.Utils;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.List;
import java.util.Optional;

public class GetQuestionInfosAction extends TransactionalAction {
    private final List<String> questionIds;

    public GetQuestionInfosAction(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    public boolean shouldLogging() {
        return false;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        var questions = MultiPartitionableQuestionService.singletonInstance.findAllById(questionIds);
        JsonObject result = new JsonObject();
        JsonArray questionArray = new JsonArray(questions.size());
        for (MultiPartitionableQuestion question : questions) {
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
