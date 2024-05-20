package indi.etern.checkIn.api.webSocket.action.question.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.List;

public class Utils {
    public static void sendDeleteQuestionToAll(String questionID) {
        JsonObject jsonObject = new JsonObject();
//        Map<String, Object> dataMap = new HashMap<>();
        jsonObject.addProperty("type", "deleteQuestion");
        jsonObject.addProperty("id", questionID);
//        dataMap.put("type", "deleteQuestion");
//        dataMap.put("questionID", questionID);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
    
    public static void sendUpdateQuestionsToAll(List<MultiPartitionableQuestion> questions) {
        JsonArray jsonArray = new JsonArray();
        for (MultiPartitionableQuestion question : questions) {
            JsonObject questionObj = new JsonObject();
            questionObj.addProperty("id",question.getId());
            questionObj.addProperty("content", question.getContent());
            questionObj.addProperty("enabled", question.isEnabled());
            questionObj.addProperty("type", question.getClass().getSimpleName());
            JsonArray partitions = new JsonArray();
            question.getPartitions().forEach(partition -> partitions.add(partition.getId()));
            questionObj.add("partitionIds", partitions);
            jsonArray.add(questionObj);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "updateQuestions");
        jsonObject.add("questions", jsonArray);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}
