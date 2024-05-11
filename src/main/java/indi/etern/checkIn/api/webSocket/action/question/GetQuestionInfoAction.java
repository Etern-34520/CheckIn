package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.*;

public class GetQuestionInfoAction extends JsonResultAction {
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
        //FIXME 无法获取图片
        Optional<MultiPartitionableQuestion> questionOptional = MultiPartitionableQuestionService.singletonInstance.findById(questionId);
        if (questionOptional.isEmpty()) {
            JsonObject notFound = new JsonObject();
            notFound.addProperty("type", "question not found");
            return Optional.of(notFound);
        } else {
            MultiPartitionableQuestion question = questionOptional.get();
            JsonObject result = new JsonObject();
            result.addProperty("id", question.getId());
            result.addProperty("type", question.getClass().getSimpleName());
            result.addProperty("content", question.getContent());
            result.addProperty("enabled", question.isEnabled());
            result.addProperty("lastModifiedTime", question.getLastModifiedTimeString());
            if (question instanceof MultipleChoiceQuestion multipleChoiceQuestion) {
                JsonArray choices = new JsonArray();
                List<String> correctIds = new ArrayList<>(1);
                int index = 0;
                for (Choice choice : multipleChoiceQuestion.getChoices()) {
                    JsonObject choiceJson = new JsonObject();
                    choiceJson.addProperty("id", choice.getId());
                    choiceJson.addProperty("content", choice.getContent());
                    boolean correct = choice.isCorrect();
                    choiceJson.addProperty("correct", correct);
                    if (correct) {
                        correctIds.add(choice.getId());
                    }
                    choices.add(choiceJson);
                    index++;
                }
                result.add("choices", choices);
                if (correctIds.size() == 1) {
                    result.addProperty("correctChoiceId", correctIds.getFirst());
                } else if (correctIds.size() > 1) {
                    JsonArray correctIdsJson = new JsonArray();
                    for (String correctId : correctIds) {
                        correctIdsJson.add(correctId);
                    }
                    result.add("correctChoiceIds", correctIdsJson);
                }
            }
            if (question instanceof ImagesWith imagesWith) {
                JsonArray images = new JsonArray();
                for (Map.Entry<String,String> imageEntry : imagesWith.getImageBase64Strings().entrySet()) {
                    JsonObject imageInfo = new JsonObject();
                    imageInfo.addProperty("name", imageEntry.getKey());
                    imageInfo.addProperty("size", imageEntry.getValue().length());
                    imageInfo.addProperty("url", imageEntry.getValue());
                    images.add(imageInfo);
                }
                result.add("images", images);
            }
            Set<Partition> partitions1 = question.getPartitions();
            JsonArray partitionIds = new JsonArray(partitions1.size());
            for (Partition partition : partitions1) {
                partitionIds.add(partition.getId());
            }
            result.add("partitionIds", partitionIds);
            User author = question.getAuthor();
            result.addProperty("authorQQ", author==null?null:author.getQQNumber());
            return Optional.of(result);
        }
    }

    @Override
    public Optional<JsonObject> logMessage(Optional<JsonObject> result) {
        //noinspection OptionalGetWithoutIsPresent Impossible to be null
        JsonObject jsonObject = result.get();
        JsonArray images = ((JsonArray)jsonObject.get("images"));
        for (JsonElement image : images) {
            JsonObject imageObject = (JsonObject) image;
            imageObject.remove("url");
            imageObject.addProperty("url", "[ image base64 url (masked due to length) ]");
        }
        return Optional.of(jsonObject);
    }
}
