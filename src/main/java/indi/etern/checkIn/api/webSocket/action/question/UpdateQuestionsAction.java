package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.Connector;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static indi.etern.checkIn.api.webSocket.action.question.utils.Utils.sendUpdateQuestionsToAll;

public class UpdateQuestionsAction extends TransactionalAction {
    boolean allOwned = true;
    boolean switchEnabled = false;
    List<Object> questions;

    public UpdateQuestionsAction(List<Object> questions, long authorQQ) {
        this.questions = questions;
/*
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
*/
    }


    @Override
    public String requiredPermissionName() {
        return allOwned ? "create and edit owns question" : "edit others question" + (switchEnabled ? ",enable and disable question" : "");
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        List<MultiPartitionableQuestion> succeedQuestions = new ArrayList<>();
        for (Object questionObj : questions) {
            if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                try {
//                String type = questionDataMap.get("type").toString();
//                if (type.startsWith("SingleCorrectQuestion") ||
//                        type.startsWith("MultipleCorrectQuestion")) {
                    String type = (String) questionDataMap.get("type");
                    MultiPartitionableQuestion multiPartitionableQuestion;
                    switch (type) {
                        case "MultipleChoicesQuestion" -> multiPartitionableQuestion = createMultipleChoicesQuestion(questionDataMap);
                        case "QuestionGroup" -> multiPartitionableQuestion = createQuestionGroup(questionDataMap);
                        default -> throw new UnsupportedOperationException("type not supported: "+type);
                    }
//                    MultiPartitionableQuestionService.singletonInstance.unbindAndDeleteById(multiPartitionableQuestion.getId());
                    MultiPartitionableQuestionService.singletonInstance.update(multiPartitionableQuestion);
                    succeedQuestions.add(multiPartitionableQuestion);
                } catch (Exception e) {
                    Connector.logger.error("UpdateQuestionsAction.doAction", e);
                }
            }
        }
        MultiPartitionableQuestionService.singletonInstance.flush();
        sendUpdateQuestionsToAll(succeedQuestions);
        return Optional.empty();
    }

    private MultiPartitionableQuestion createQuestionGroup(Map questionDataMap) {
        return null;
    }

    private static MultiPartitionableQuestion createMultipleChoicesQuestion(Map questionDataMap) {
        String id = (String) questionDataMap.get("id");
        Optional<MultiPartitionableQuestion> questionOptional = MultiPartitionableQuestionService.singletonInstance.findById(id);

        MultipleQuestionBuilder multipleQuestionBuilder;
        if (questionOptional.isEmpty()) {
            multipleQuestionBuilder = new MultipleQuestionBuilder();
        } else {
            multipleQuestionBuilder = MultipleQuestionBuilder.from((MultipleChoicesQuestion) questionOptional.get());
        }
        multipleQuestionBuilder.setId(id);

        Object o = questionDataMap.get("content");
        if (o instanceof String) {
            String content = (String) o;
            multipleQuestionBuilder.setQuestionContent(content);
        }

        Object o1 = questionDataMap.get("enabled");
        if (o1 instanceof Boolean) {
            boolean enabled = (boolean) o1;
            multipleQuestionBuilder.setEnable(enabled);
        }

        Object o2 = questionDataMap.get("choices");
        if (o2 instanceof List) {
            multipleQuestionBuilder.getChoices().clear();
            //noinspection unchecked
            List<Map<String, Object>> choices = (List<Map<String, Object>>) o2;
            for (Map<String, Object> choice : choices) {
                String choiceContent = (String) choice.get("content");
                boolean correct = (boolean) choice.get("correct");
                multipleQuestionBuilder.addChoice(new Choice(choiceContent, correct));
            }
        }

        Object o3 = questionDataMap.get("partitionIds");
        if (o3 instanceof List) {
            multipleQuestionBuilder.getPartitions().clear();
            //noinspection unchecked
            List<Double> partitionIds = (List<Double>) o3;
            for (Double partitionId : partitionIds) {
                multipleQuestionBuilder.addPartition((int) partitionId.longValue());
            }
        }

        Object authorQQObj = questionDataMap.get("authorQQ");
        if (authorQQObj != null) {
            long authorQQ = ((Double) authorQQObj).longValue();
            multipleQuestionBuilder.setAuthor(UserService.singletonInstance.findByQQNumber(authorQQ).orElse(null));
        }

        Object imageBase64Strings1 = questionDataMap.get("images");
        if (imageBase64Strings1 instanceof List) {
            multipleQuestionBuilder.getImageBase64Strings().clear();
            //noinspection unchecked
            List<Map<String, String>> imageBase64Strings = (List<Map<String, String>>) imageBase64Strings1;
            for (Map<String, String> imageBase64String : imageBase64Strings) {
                String key = imageBase64String.get("name");
                String value = imageBase64String.get("url");
                multipleQuestionBuilder.addBase64Image(key, value);
            }
        }

        return multipleQuestionBuilder.build();
    }

    @Override
    public boolean shouldLogging() {
        return false;
    }
}
