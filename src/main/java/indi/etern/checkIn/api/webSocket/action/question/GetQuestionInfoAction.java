package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GetQuestionInfoAction extends JsonResultAction {
    private final String questionId;
    public GetQuestionInfoAction(String questionId) {
        this.questionId = questionId;
    }

    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<MultiPartitionableQuestion> questionOptional = MultiPartitionableQuestionService.singletonInstance.findById(questionId);
        if (questionOptional.isEmpty()) {
            return Optional.empty();
        } else {
            MultiPartitionableQuestion question = questionOptional.get();
            JsonObject result = new JsonObject();
            result.addProperty("id", question.getId());
            result.addProperty("type", question.getClass().getSimpleName());
            result.addProperty("content", question.getContent());
            if (question instanceof MultipleChoiceQuestion multipleChoiceQuestion) {
                JsonArray choices = new JsonArray();
                List<String> correctIds = new ArrayList<>(1);
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
                int index = 0;
                for (String imagePathString : imagesWith.getImagePathStrings()) {
                    JsonObject imageInfo = new JsonObject();
                    final String imageName = imagePathString.substring(imagePathString.lastIndexOf('/') + 1);

                    imageInfo.addProperty("name",imageName);

                    byte[] bytes;
                    try {
                        final InputStream inputStream = getImageInputStreamOf(imagesWith, index);
                        bytes = IOUtils.toByteArray(inputStream);

                        imageInfo.addProperty("size",bytes.length);

                        inputStream.close();
                        String type;
                        if (imageName.endsWith(".jpg") || imageName.endsWith(".jpeg"))
                            type = "data:image/jpeg;base64,";
                        else if (imageName.endsWith(".png"))
                            type = "data:image/png;base64,";
                        else if (imageName.endsWith(".gif"))
                            type = "data:image/gif;base64,";
                        else
                            type = "data:image;base64,";

                        imageInfo.addProperty("url", type + Base64.getEncoder().encodeToString(bytes));
                        images.add(imageInfo);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    index++;
                }
                result.add("images", images);
            }
            return Optional.of(result);
        }
    }

    private InputStream getImageInputStreamOf(ImagesWith questionWithImage, int imageIndex) {
        try {
            return new FileInputStream(questionWithImage.getImagePathStrings().get(imageIndex));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
