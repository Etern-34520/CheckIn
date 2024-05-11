package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UpdateQuestionsAction extends QuestionAction {
    boolean shouldLogging = false;
    boolean allOwned = true;
    boolean switchEnabled = false;
    List<Object> questions;
    public UpdateQuestionsAction(List<Object> questions, long authorQQ) {
        this.questions = questions;
        for (Object questionObj : questions) {
            if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                if (allOwned && questionDataMap.get("authorQQ") != null) {
                    long authorQQ1 = ((Double) questionDataMap.get("authorQQ")).longValue();
                    if (authorQQ1 != authorQQ) {
                        allOwned = false;
                        break;
                    }
                }
                if (!switchEnabled) {
                    Optional<MultiPartitionableQuestion> formerQuestion = MultiPartitionableQuestionService.singletonInstance.findById(((Map<?, ?>) questionObj).get("id").toString());
                    if (formerQuestion.isPresent() && ((boolean) ((Map<?, ?>) questionObj).get("enabled")) != formerQuestion.get().isEnabled()) {
                        switchEnabled = true;
                    }
                }
            }
        }
    }

    @Override
    public String requiredPermissionName() {
        return allOwned?"create and edit owns question":"edit others question" + (switchEnabled?",enable and disable question":"");
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        for (Object questionObj : questions) {
            if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                String type = questionDataMap.get("type").toString();
                if (type.startsWith("SingleCorrectQuestion") ||
                        type.startsWith("MultipleCorrectQuestion")) {
                    String content = (String) questionDataMap.get("content");
                    String id = (String) questionDataMap.get("id");
                    boolean enabled = (boolean) questionDataMap.get("enabled");
                    //noinspection unchecked
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) questionDataMap.get("choices");
                    //noinspection unchecked
                    List<Double> partitionIds = (List<Double>) questionDataMap.get("partitionIds");
                    Object authorQQObj = questionDataMap.get("authorQQ");
                    MultipleQuestionBuilder multipleQuestionBuilder = new MultipleQuestionBuilder();
                    multipleQuestionBuilder.setId(id)
                            .setQuestionContent(content)
                            .setEnable(enabled);
                    for (Double partitionId : partitionIds) {
                        multipleQuestionBuilder.addPartition((int) partitionId.longValue());
                    }
                    if (authorQQObj != null) {
                        long authorQQ = ((Double) authorQQObj).longValue();
                        multipleQuestionBuilder.setAuthor(UserService.singletonInstance.findByQQNumber(authorQQ).orElse(null));
                    }
                    for (Map<String, Object> choice : choices) {
                        String choiceContent = (String) choice.get("content");
                        boolean correct = (boolean) choice.get("correct");
                        multipleQuestionBuilder.addChoice(new Choice(choiceContent, correct));
                    }
                    Object imageBase64Strings1 = questionDataMap.get("images");
                    if (imageBase64Strings1 instanceof List) {
                        //noinspection unchecked
                        List<Map<String, String>> imageBase64Strings = (List<Map<String, String>>) imageBase64Strings1;
                        for (Map<String, String> imageBase64String : imageBase64Strings) {
                            String key = imageBase64String.get("name");
                            String value = imageBase64String.get("url");
                            multipleQuestionBuilder.addBase64Image(key, value);
                        }
                    }
                    MultiPartitionableQuestion multiPartitionableQuestion = multipleQuestionBuilder.build();
                    MultiPartitionableQuestionService.singletonInstance.deleteById(multiPartitionableQuestion.getId());
                    MultiPartitionableQuestionService.singletonInstance.save(multiPartitionableQuestion);
                }
            }
        }
        MultiPartitionableQuestionService.singletonInstance.flush();
        return Optional.empty();
    }

    @Override
    public boolean shouldLogging() {
        return shouldLogging;
    }
}
