package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.DeleteQuestionAction;
import indi.etern.checkIn.action.question.utils.Utils;
import indi.etern.checkIn.api.webSocket.Connector;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static indi.etern.checkIn.action.question.utils.Utils.sendDeleteQuestionsToAll;


@Action("updateQuestions")
public class UpdateQuestionsAction extends TransactionalAction {
    boolean allOwned = true;
    boolean switchEnabled = false;
    List<Object> questions;
    @Autowired
    ActionExecutor actionExecutor;
    private List<String> deletedQuestionIds;

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
                    Optional<Question> formerQuestion = QuestionService.singletonInstance.findById(((Map<?, ?>) questionObj).get("id").toString());
                    if (formerQuestion.isPresent() && ((boolean) ((Map<?, ?>) questionObj).get("enabled")) != formerQuestion.get().isEnabled()) {
                        switchEnabled = true;
                    }
                }
            }
        }
*/
    
    
    @Override
    public String requiredPermissionName() {
        return allOwned ? "create and edit owns question" : "edit others question" + (switchEnabled ? ",enable and disable question" : "");
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<Question> succeedQuestions = new ArrayList<>();
        if (questions != null)
            for (Object questionObj : questions) {
                if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                    try {
//                String type = questionDataMap.get("type").toString();
//                if (type.startsWith("SingleCorrectQuestion") ||
//                        type.startsWith("MultipleCorrectQuestion")) {
                        String type = (String) questionDataMap.get("type");
                        Question multiPartitionableQuestion;
                        switch (type) {
                            case "MultipleChoicesQuestion" -> {
//                            multiPartitionableQuestion = createMultipleChoicesQuestion(questionDataMap);
                                @SuppressWarnings("unchecked") Optional<Question> result =
                                        (Optional<Question>) actionExecutor.executeByTypeClass(CreateOrUpdateQuestionAction.class, questionDataMap);
                                multiPartitionableQuestion = result.orElse(null);
                            }
                            case "QuestionGroup" -> {
//                            multiPartitionableQuestion = createQuestionGroup(questionDataMap);
                                @SuppressWarnings("unchecked") Optional<Question> result =
                                        (Optional<Question>) actionExecutor.executeByTypeClass(CreateOrUpdateQuestionGroup.class, questionDataMap);
                                multiPartitionableQuestion = result.orElse(null);
                            }
                            default -> throw new UnsupportedOperationException("type not supported: " + type);
                        }
//                    QuestionService.singletonInstance.unbindAndDeleteById(multiPartitionableQuestion.getId());
//                    QuestionService.singletonInstance.update(multiPartitionableQuestion);
                        succeedQuestions.add(multiPartitionableQuestion);
                    } catch (Exception e) {
                        Connector.logger.error("UpdateQuestionsAction.doAction", e);
                    }
                }
            }
        if (deletedQuestionIds != null) {
            List<String> succeedDeletedQuestions = new ArrayList<>();
            for (String deletedQuestionId : deletedQuestionIds) {
                @SuppressWarnings("unchecked") Optional<String> result =
                        (Optional<String>) actionExecutor.executeByTypeClass(DeleteQuestionAction.class, deletedQuestionId);
                succeedDeletedQuestions.add(result.orElse(null));
            }
            sendDeleteQuestionsToAll(succeedDeletedQuestions);
        }
        QuestionService.singletonInstance.flush();
        Utils.sendUpdateQuestionsToAll(succeedQuestions);
        return Optional.ofNullable(getSuccessMap());
    }
    
    private Question createQuestionGroup(Map<?, ?> questionDataMap) {
        return null;
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        final Object updatedQuestions = dataMap.get("updatedQuestions");
        if (updatedQuestions instanceof List<?>) {
            //noinspection unchecked
            questions = (List<Object>) updatedQuestions;
        }
        final Object deletedQuestionIds1 = dataMap.get("deletedQuestionIds");
        if (deletedQuestionIds1 instanceof List<?>) {
            //noinspection unchecked
            deletedQuestionIds = (List<String>) deletedQuestionIds1;
        }
    }
}