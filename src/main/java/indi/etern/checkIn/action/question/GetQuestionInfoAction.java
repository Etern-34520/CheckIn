package indi.etern.checkIn.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.utils.Utils;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.*;

@Action(name = "getQuestionInfo")
public class GetQuestionInfoAction extends TransactionalAction {
    private String questionId;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(questionId);
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

    @Override
    public void initData(Map<String, Object> dataMap) {
        questionId = (String) dataMap.get("questionId");
    }
}
