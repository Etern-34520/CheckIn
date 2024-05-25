package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.api.webSocket.action.question.utils.Utils;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.*;

public class GetQuestionInfoAction extends TransactionalAction {
    private final String questionId;
    public GetQuestionInfoAction(String questionId) {
        this.questionId = questionId;
    }

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<MultiPartitionableQuestion> questionOptional = MultiPartitionableQuestionService.singletonInstance.findById(questionId);
        if (questionOptional.isEmpty()) {
            JsonObject notFound = new JsonObject();
            notFound.addProperty("type", "question not found");
            return Optional.of(notFound);
        } else {
            return Optional.of(Utils.getJsonObjectOf(questionOptional.get()));
        }
    }

    @Override
    public Optional<JsonObject> logMessage(Optional<JsonObject> result) {
        //noinspection OptionalGetWithoutIsPresent Impossible to be null
        JsonObject jsonObject = result.get();
        JsonElement images1 = jsonObject.get("images");
        if (images1 instanceof JsonArray images) {
            for (JsonElement image : images) {
                JsonObject imageObject = (JsonObject) image;
                imageObject.remove("url");
                imageObject.addProperty("url", "[ image base64 url (masked due to length) ]");
            }
        }
        return Optional.of(jsonObject);
    }
}
