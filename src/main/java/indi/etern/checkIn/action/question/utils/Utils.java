package indi.etern.checkIn.action.question.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.linkUtils.Link;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.question.impl.question.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {
    public static void sendDeleteQuestionToAll(String questionID) {
        JsonObject jsonObject = new JsonObject();
//        Map<String, Object> dataMap = new HashMap<>();
        jsonObject.addProperty("type", "deleteQuestion");
        jsonObject.addProperty("id", questionID);
//        dataMap.put("type", "deleteQuestion");
//        dataMap.put("questionID", questionID);
        WebSocketService.singletonInstance.sendMessageToAllWithoutLog(jsonObject);
    }

    public static void sendUpdateQuestionsToAll(List<Question> questions) {
        JsonArray jsonArray = new JsonArray();
        for (Question question : questions) {
            JsonObject questionObj = new JsonObject();
            questionObj.addProperty("id", question.getId());
            questionObj.addProperty("content", question.getContent());
            questionObj.addProperty("enabled", question.isEnabled());
            questionObj.addProperty("type", question.getClass().getSimpleName());
            JsonArray partitions = new JsonArray();
            final Link<?, ?> link = question.getLinkWrapper();
            if (link instanceof ToPartitionLink linkWrapper1) {
                linkWrapper1.getTargets().forEach(partition -> partitions.add(partition.getId()));
            }
//            link.getPartitions().forEach(partition -> partitions.add(partition.getId()));
            questionObj.add("partitionIds", partitions);
            jsonArray.add(questionObj);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "updateQuestions");
        jsonObject.add("questions", jsonArray);
        WebSocketService.singletonInstance.sendMessageToAllWithoutLog(jsonObject);
    }

    public static void sendDeleteQuestionsToAll(List<String> questionIds) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (String questionId : questionIds) {
            jsonArray.add(questionId);
        }
        jsonObject.addProperty("type", "deleteQuestions");
        jsonObject.add("ids", jsonArray);
        WebSocketService.singletonInstance.sendMessageToAllWithoutLog(jsonObject);
    }

    public static JsonObject getJsonObjectOf(Question question) {
//            Question question = questionOptional.get();
        JsonObject result = new JsonObject();
        result.addProperty("id", question.getId());
        result.addProperty("type", question.getClass().getSimpleName());
        result.addProperty("content", question.getContent());
        result.addProperty("enabled", question.isEnabled());
        result.addProperty("lastModifiedTime", question.getLastModifiedTimeString());
        if (question instanceof MultipleChoicesQuestion multipleChoiceQuestion) {
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
        } else if (question instanceof QuestionGroup questionGroup) {
            Set<ToQuestionGroupLink> questionLinks = questionGroup.getQuestionLinks();
            JsonArray subQuestions = new JsonArray(questionLinks.size());
            for (ToQuestionGroupLink questionLink : questionLinks) {
                subQuestions.add(getJsonObjectOf(questionLink.getSource()));
            }
            result.add("questions",subQuestions);
        }
        if (question.getImageBase64Strings() != null) {
            JsonArray images = new JsonArray();
            for (Map.Entry<String, String> imageEntry : question.getImageBase64Strings().entrySet()) {
                JsonObject imageInfo = new JsonObject();
                imageInfo.addProperty("name", imageEntry.getKey());
                imageInfo.addProperty("size", imageEntry.getValue().length());
                imageInfo.addProperty("url", imageEntry.getValue());
                images.add(imageInfo);
            }
            result.add("images", images);
        }
        final Link<?, ?> link = question.getLinkWrapper();
        if (link instanceof ToPartitionLink toPartitionLinkWrapper) {
            Set<Partition> partitions1 = toPartitionLinkWrapper.getTargets();
            JsonArray partitionIds = new JsonArray(partitions1.size());
            for (Partition partition : partitions1) {
                partitionIds.add(partition.getId());
            }
            result.add("partitionIds", partitionIds);
        }

        Set<User> upVoters = question.getUpVoters();
        JsonArray upvoterIds = new JsonArray(upVoters.size());
        for (User user : upVoters) {
            upvoterIds.add(user.getQQNumber());
        }
        result.add("upVoters", upvoterIds);

        Set<User> downVoters = question.getDownVoters();
        JsonArray downvoterIds = new JsonArray(downVoters.size());
        for (User user : downVoters) {
            downvoterIds.add(user.getQQNumber());
        }
        result.add("downVoters", downvoterIds);

        User author = question.getAuthor();
        result.addProperty("authorQQ", author == null ? null : author.getQQNumber());
        return result;
//        }
    }
}
