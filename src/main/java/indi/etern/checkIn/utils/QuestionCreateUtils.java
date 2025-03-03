package indi.etern.checkIn.utils;

import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.question.impl.question.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestionCreateUtils {
    @FunctionalInterface
    private interface LinkHandler {
        void handle(Map<?, ?> questionDataMap, MultipleChoicesQuestion.Builder builder);
    }
    
    private static MultipleChoicesQuestion create(Map<?, ?> questionDataMap, LinkHandler linkHandler) {
        String id = (String) questionDataMap.get("id");
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(id);
        
        MultipleChoicesQuestion.Builder builder;
        if (questionOptional.isEmpty()) {
            builder = new MultipleChoicesQuestion.Builder();
        } else {
            builder = MultipleChoicesQuestion.Builder.from((MultipleChoicesQuestion) questionOptional.get());
        }
        builder.setId(id);
        
        Object content1 = questionDataMap.get("content");
        if (content1 instanceof String) {
            String content = (String) content1;
            builder.setQuestionContent(content);
        }
        
        Object randomOrdered = questionDataMap.get("randomOrdered");
        if (randomOrdered != null) {
            builder.setRandomOrdered((boolean) randomOrdered);
        }
        
        Object enabled1 = questionDataMap.get("enabled");
        if (enabled1 instanceof Boolean) {
            boolean enabled = (boolean) enabled1;
            builder.setEnable(enabled);
        }
        
        Object choices1 = questionDataMap.get("choices");
        if (choices1 instanceof List) {
            builder.getChoices().clear();
            //noinspection unchecked
            List<Map<String, Object>> choices = (List<Map<String, Object>>) choices1;
            for (Map<String, Object> choice : choices) {
                String choiceContent = (String) choice.get("content");
                boolean correct = (boolean) choice.get("correct");
                builder.addChoice(new Choice(choiceContent, correct));
            }
        }
        
        linkHandler.handle(questionDataMap, builder);
        
        Object authorQQObj = questionDataMap.get("authorQQ");
        if (authorQQObj instanceof Number authorQQNumberValue) {
            long authorQQ = authorQQNumberValue.longValue();
            builder.setAuthor(UserService.singletonInstance.findByQQNumber(authorQQ).orElse(null));
        }
        
        Object imageBase64Strings1 = questionDataMap.get("images");
        if (imageBase64Strings1 instanceof List) {
            builder.getImageBase64Strings().clear();
            //noinspection unchecked
            List<Map<String, String>> imageBase64Strings = (List<Map<String, String>>) imageBase64Strings1;
            for (Map<String, String> imageBase64String : imageBase64Strings) {
                String key = imageBase64String.get("name");
                String value = imageBase64String.get("url");
                builder.addBase64Image(key, value);
            }
        }
        return builder.build();
    }
    
    
    public static MultipleChoicesQuestion createMultipleChoicesQuestion(Map<?, ?> questionDataMap) {
        return create(questionDataMap, (questionDataMap1, builder) -> {
            Object o3 = questionDataMap1.get("partitionIds");
            if (o3 instanceof List) {
                //noinspection unchecked
                List<Number> partitionIds = (List<Number>) o3;
                builder.usePartitionLinks(linkWrapper -> {
                    for (Number partitionId : partitionIds) {
                        linkWrapper.getTargets().add(Partition.getInstance(partitionId.intValue()));
                    }
                });
            }
        });
    }
    
    protected static MultipleChoicesQuestion createSubMultipleChoicesQuestion(Map<?, ?> questionDataMap, QuestionGroup questionGroup) {
        return create(questionDataMap, (questionDataMap1, builder1) -> {
            builder1.useQuestionGroupLinks(linkWrapper -> {
                linkWrapper.setTarget(questionGroup);
            });
        });
    }
    
    public static QuestionGroup createQuestionGroup(Map<?, ?> questionDataMap) {
        String id = (String) questionDataMap.get("id");
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(id);
        QuestionGroup.Builder builder;
        if (questionOptional.isPresent() && questionOptional.get() instanceof QuestionGroup previousQuestionGroup) {
            builder = QuestionGroup.Builder.from(previousQuestionGroup);
        } else {
            builder = new QuestionGroup.Builder();
        }
        builder.setId((String) questionDataMap.get("id"));
        
        Object contentObj = questionDataMap.get("content");
        if (contentObj != null) {
            builder.setContent((String) contentObj);
        }
        
        Object randomOrdered = questionDataMap.get("randomOrdered");
        if (randomOrdered != null) {
            builder.setRandomOrdered((boolean) randomOrdered);
        }
        
        Object partitionIdsObj = questionDataMap.get("partitionIds");
        if (partitionIdsObj instanceof List) {
            builder.getPartitions().clear();
            //noinspection unchecked
            List<Integer> partitionIds = (List<Integer>) partitionIdsObj;
            for (Integer partitionId : partitionIds) {
                builder.addPartition(Partition.getInstance(partitionId));
            }
        }
        
        Object authorQQObj = questionDataMap.get("authorQQ");
        if (authorQQObj instanceof Number authorQQNumber) {
            long authorQQ = authorQQNumber.longValue();
            builder.setAuthor(UserService.singletonInstance.findByQQNumber(authorQQ).orElse(null));
        }
        
        Object enabledObj = questionDataMap.get("enabled");
        if (enabledObj instanceof Boolean enabled) {
            builder.setEnabled(enabled);
        }
        
        Object questionInfosObj = questionDataMap.get("questionInfos");
        if (questionInfosObj instanceof List<?>) {
            builder.getQuestions().clear();
        }
        Object imageBase64Strings1 = questionDataMap.get("images");
        if (imageBase64Strings1 instanceof List) {
            builder.getImageBase64Strings().clear();
            //noinspection unchecked
            List<Map<String, String>> imageBase64Strings = (List<Map<String, String>>) imageBase64Strings1;
            for (Map<String, String> imageBase64String : imageBase64Strings) {
                String key = imageBase64String.get("name");
                String value = imageBase64String.get("url");
                builder.addBase64Image(key, value);
            }
        }
        QuestionGroup questionGroup = builder.build();
        if (questionInfosObj instanceof List<?> questionInfos) {
            for (Object questionInfoObj : questionInfos) {
                if (questionInfoObj instanceof Map<?, ?> questionInfo) {
                    MultipleChoicesQuestion multipleChoicesQuestion = createSubMultipleChoicesQuestion((Map<?, ?>) questionInfo.get("question"), questionGroup);
                    questionGroup.addQuestion(multipleChoicesQuestion);
                }
            }
        }
        return questionGroup;
    }
}