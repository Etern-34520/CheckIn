package indi.etern.checkIn.utils;

import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.linkUtils.Link;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.entities.question.impl.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.interfaces.RandomOrderable;
import indi.etern.checkIn.entities.question.statistic.QuestionStatistic;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionStatisticService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.*;

public class QuestionUpdateUtils {
    public static void sendUpdateQuestionsToAll(List<Question> questions) {
        List<Object> list = new ArrayList<>();
        for (Question question : questions) {
            LinkedHashMap<String,Object> questionObj = new LinkedHashMap<>();
            questionObj.put("id", question.getId());
            questionObj.put("content", question.getContent());
            questionObj.put("enabled", question.isEnabled());
            questionObj.put("type", question.getClass().getSimpleName());
            questionObj.put("lastModifiedTime", question.getLastModifiedTimeString());
            ArrayList<Object> partitions = new ArrayList<>();
            final Link<?, ?> link = question.getLinkWrapper();
            if (link instanceof ToPartitionsLink linkWrapper1) {
                linkWrapper1.getTargets().forEach(partition -> partitions.add(partition.getId()));
            }
//            link.getPartitions().forEach(partition -> partitions.add(partition.getId()));
            questionObj.put("partitionIds", partitions);
            list.add(questionObj);
        }
        
        Message<?> message = Message.of("updateQuestions",list);
        WebSocketService.singletonInstance.sendMessageToAll(message);
    }

    public static LinkedHashMap<String,Object> getMapOfQuestion(Question question) {
//            Question question = questionOptional.get();
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        result.put("id", question.getId());
        result.put("type", question.getClass().getSimpleName());
        result.put("content", question.getContent());
        result.put("enabled", question.isEnabled());
        if (question instanceof RandomOrderable randomOrderable) {
            result.put("randomOrdered", randomOrderable.isRandomOrdered());
        }
        result.put("lastModifiedTime", question.getLastModifiedTimeString());
        if (question instanceof MultipleChoicesQuestion multipleChoiceQuestion) {
            ArrayList<Object> choices = new ArrayList<>();
//            List<String> correctIds = new java.util.ArrayList<>(1);
            for (Choice choice : multipleChoiceQuestion.getChoices()) {
                LinkedHashMap<String,Object> choiceMap = new LinkedHashMap<>();
                choiceMap.put("id", choice.getId());
                choiceMap.put("content", choice.getContent());
                boolean correct = choice.isCorrect();
                choiceMap.put("correct", correct);
//                if (correct) {
//                    correctIds.add(choice.getId());
//                }
                choices.add(choiceMap);
            }
            result.put("choices", choices);
//            if (correctIds.size() == 1) {
//                result.put("correctChoiceId", correctIds.getFirst());
//            } else if (correctIds.size() > 1) {
//                ArrayList<Object> correctIdsList = new ArrayList<>();
//                correctIdsList.addAll(correctIds);
//                result.put("correctChoiceIds", correctIdsList);
//            }
        } else if (question instanceof QuestionGroup questionGroup) {
            Set<ToQuestionGroupLink> questionLinks = questionGroup.getQuestionLinks();
            ArrayList<Object> subQuestions = new ArrayList<>(questionLinks.size());
            for (ToQuestionGroupLink questionLink : questionLinks) {
                subQuestions.add(getMapOfQuestion(questionLink.getSource()));
            }
            result.put("questions",subQuestions);
        }
        if (question.getImageBase64Strings() != null) {
            ArrayList<Object> images = new ArrayList<>();
            for (Map.Entry<String, String> imageEntry : question.getImageBase64Strings().entrySet()) {
                LinkedHashMap<String,Object> imageInfo = new LinkedHashMap<>();
                imageInfo.put("name", imageEntry.getKey());
                imageInfo.put("size", imageEntry.getValue().length());
                imageInfo.put("url", imageEntry.getValue());
                images.add(imageInfo);
            }
            result.put("images", images);
        }
        final Link<?, ?> link = question.getLinkWrapper();
        if (link instanceof ToPartitionsLink toPartitionsLinkWrapper) {
            Set<Partition> partitions1 = toPartitionsLinkWrapper.getTargets();
            ArrayList<Object> partitionIds = new ArrayList<>(partitions1.size());
            for (Partition partition : partitions1) {
                partitionIds.add(partition.getId());
            }
            result.put("partitionIds", partitionIds);
        }

        Set<User> upVoters = question.getUpVoters();
        ArrayList<Object> upvoterIds = new ArrayList<>(upVoters.size());
        for (User user : upVoters) {
            upvoterIds.add(user.getQQNumber());
        }
        result.put("upVoters", upvoterIds);

        Set<User> downVoters = question.getDownVoters();
        ArrayList<Object> downvoterIds = new ArrayList<>(downVoters.size());
        for (User user : downVoters) {
            downvoterIds.add(user.getQQNumber());
        }
        result.put("downVoters", downvoterIds);

        User author = question.getAuthor();
        result.put("authorQQ", author == null ? null : author.getQQNumber());
        
        final Map<String, Number> statisticMap = getStatisticMap(question);
        result.put("statistic", statisticMap);
        return result;
//        }
    }
    
    private static Map<String, Number> getStatisticMap(Question question) {
        Optional<QuestionStatistic> questionStatisticOptional = QuestionStatisticService.singletonInstance.findById(question.getId());
        if (questionStatisticOptional.isEmpty()) {
            return null;
        } else {
            final QuestionStatistic questionStatistic = questionStatisticOptional.get();
            Map<String,Number> statisticMap = new LinkedHashMap<>();
            statisticMap.put("drewCount",questionStatistic.getDrewCount());
            statisticMap.put("submittedCount",questionStatistic.getSubmittedCount());
            statisticMap.put("correctCount",questionStatistic.getCorrectCount());
            statisticMap.put("wrongCount",questionStatistic.getWrongCount());
            statisticMap.put("examDataCount",questionStatistic.getDrewExamData().size());
            return statisticMap;
        }
    }
    
}
