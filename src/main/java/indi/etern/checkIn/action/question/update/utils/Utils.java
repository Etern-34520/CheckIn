package indi.etern.checkIn.action.question.update.utils;

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

public class Utils {
    @FunctionalInterface
    private interface LinkHandler {
        void handle(Map<?,?> questionDataMap,MultipleChoicesQuestion.Builder builder);
    }

    private static MultipleChoicesQuestion create(Map<?,?> questionDataMap,LinkHandler linkHandler) {
        String id = (String) questionDataMap.get("id");
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(id);

        MultipleChoicesQuestion.Builder builder;
        if (questionOptional.isEmpty()) {
            builder = new MultipleChoicesQuestion.Builder();
        } else {
            builder = MultipleChoicesQuestion.Builder.from((MultipleChoicesQuestion) questionOptional.get());
        }
        builder.setId(id);

        Object o = questionDataMap.get("content");
        if (o instanceof String) {
            String content = (String) o;
            builder.setQuestionContent(content);
        }

        Object o1 = questionDataMap.get("enabled");
        if (o1 instanceof Boolean) {
            boolean enabled = (boolean) o1;
            builder.setEnable(enabled);
        }

        Object o2 = questionDataMap.get("choices");
        if (o2 instanceof List) {
            builder.getChoices().clear();
            //noinspection unchecked
            List<Map<String, Object>> choices = (List<Map<String, Object>>) o2;
            for (Map<String, Object> choice : choices) {
                String choiceContent = (String) choice.get("content");
                boolean correct = (boolean) choice.get("correct");
                builder.addChoice(new Choice(choiceContent, correct));
            }
        }

        linkHandler.handle(questionDataMap,builder);

        Object authorQQObj = questionDataMap.get("authorQQ");
        if (authorQQObj != null) {
            long authorQQ = ((Double) authorQQObj).longValue();
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
        return create(questionDataMap,(questionDataMap1,builder) -> {
            Object o3 = questionDataMap1.get("partitionIds");
            if (o3 instanceof List) {
                builder.getPartitions().clear();
                //noinspection unchecked
                List<Double> partitionIds = (List<Double>) o3;
                for (Double partitionId : partitionIds) {
                    builder.usePartitionLinks(linkWrapper -> {
                        linkWrapper.getTargets().add(Partition.getInstance((int) partitionId.longValue()));
                    });
                }
            }
        });
    }

    protected static MultipleChoicesQuestion createSubMultipleChoicesQuestion(Map<?, ?> questionDataMap,QuestionGroup questionGroup) {
        return create(questionDataMap,(questionDataMap1,builder1) -> {
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

        Object partitionIdsObj = questionDataMap.get("partitionIds");
        if (partitionIdsObj instanceof List) {
            //noinspection unchecked
            List<Double> partitionIds = (List<Double>) partitionIdsObj;
            for (Double partitionId : partitionIds) {
                builder.addPartition(Partition.getInstance((int) partitionId.longValue()));
            }
        }

        Object authorQQObj = questionDataMap.get("authorQQ");
        if (authorQQObj instanceof Double authorQQDoubleValue) {
            long authorQQ = authorQQDoubleValue.longValue();
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
        QuestionGroup questionGroup = builder.build();
        if (questionInfosObj instanceof List<?> questionInfos) {
            for (Object questionInfoObj : questionInfos) {
                if (questionInfoObj instanceof Map<?, ?> questionInfo) {
                    MultipleChoicesQuestion multipleChoicesQuestion = createSubMultipleChoicesQuestion((Map<?, ?>) questionInfo.get("question"),questionGroup);
                    QuestionService.singletonInstance.save(multipleChoicesQuestion);
                }
            }
        }

        return questionGroup;
    }
}