package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.List;

public abstract class QuestionAction extends JsonResultAction {
    protected void sendDeleteQuestionToAll(String questionID) {
        JsonObject jsonObject = new JsonObject();
//        Map<String, Object> dataMap = new HashMap<>();
        jsonObject.addProperty("type", "deleteQuestion");
        jsonObject.addProperty("questionID", questionID);
//        dataMap.put("type", "deleteQuestion");
//        dataMap.put("questionID", questionID);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }

    protected void sendUpdateQuestionToAll(MultiPartitionableQuestion question) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "updateQuestion");
        jsonObject.addProperty("question", question.toJsonData());
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }

    protected void sendUpdateQuestionsToAll(List<MultiPartitionableQuestion> questions) {
        JsonArray jsonArray = new JsonArray();
        for (MultiPartitionableQuestion question : questions) {
            jsonArray.add(question.toJsonData());
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "updateQuestions");
        jsonObject.add("questions", jsonArray);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}
