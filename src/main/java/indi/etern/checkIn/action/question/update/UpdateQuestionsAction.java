package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.DeleteQuestionAction;
import indi.etern.checkIn.action.question.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.utils.PartitionUpdateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Action("updateQuestions")
public class UpdateQuestionsAction extends TransactionalAction {
    boolean allOwned = true;
    boolean switchEnabled = false;
    List<Object> questions;
    @Autowired
    ActionExecutor actionExecutor;
    private List<String> deletedQuestionIds;
    private final Logger logger = LoggerFactory.getLogger(UpdateQuestionsAction.class);
    
    @Override
    public String requiredPermissionName() {
        return allOwned ? "create and edit owns question" : "edit others question" + (switchEnabled ? ",enable and disable question" : "");
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<Question> succeedQuestions = new ArrayList<>();
        Set<Partition> infectedPartitions = new HashSet<>();
        if (questions != null)
            for (Object questionObj : questions) {
                if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                    try {
                        String type = (String) questionDataMap.get("type");
                        Question question;
                        switch (type) {
                            case "MultipleChoicesQuestion" -> {
                                Optional<Question> result = actionExecutor.executeByTypeClass(CreateOrUpdateQuestion.class, questionDataMap);
                                question = result.orElse(null);
                            }
                            case "QuestionGroup" -> {
                                Optional<Question> result = actionExecutor.executeByTypeClass(CreateOrUpdateQuestionGroup.class, questionDataMap);
                                question = result.orElse(null);
                            }
                            default -> throw new UnsupportedOperationException("type not supported: " + type);
                        }
                        succeedQuestions.add(question);
                        
                        if (question != null) {
                            final QuestionLinkImpl<?> linkWrapper = question.getLinkWrapper();
                            if (linkWrapper instanceof ToPartitionsLink link) {
                                infectedPartitions.addAll(link.getTargets());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("UpdateQuestionsAction.doAction", e);
                    }
                }
            }
        if (deletedQuestionIds != null) {
            List<Question> succeedDeletedQuestions = new ArrayList<>();
            for (String deletedQuestionId : deletedQuestionIds) {
                Optional<Question> result = actionExecutor.executeByTypeClass(DeleteQuestionAction.class, deletedQuestionId);
                succeedDeletedQuestions.add(result.orElse(null));
                if (result.isPresent()) {
                    final QuestionLinkImpl<?> linkWrapper = result.get().getLinkWrapper();
                    if (linkWrapper instanceof ToPartitionsLink link) {
                        infectedPartitions.addAll(link.getTargets());
                    }
                }
            }
            QuestionUpdateUtils.sendDeleteQuestionsToAll(succeedDeletedQuestions);
        }
        QuestionService.singletonInstance.flush();
        QuestionUpdateUtils.sendUpdateQuestionsToAll(succeedQuestions);
        PartitionUpdateUtils.sendUpdatePartitionsToAll(infectedPartitions);
        return Optional.ofNullable(getSuccessMap());
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