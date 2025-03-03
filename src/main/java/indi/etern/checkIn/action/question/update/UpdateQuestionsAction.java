package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.delete.DeleteQuestionAction;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import indi.etern.checkIn.utils.PartitionUpdateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Action("updateQuestions")
public class UpdateQuestionsAction extends TransactionalAction {
    List<Object> questions;
    final ActionExecutor actionExecutor;
    final QuestionService questionService;
    final PartitionService partitionService;
    private List<String> deletedQuestionIds;
    private final Logger logger = LoggerFactory.getLogger(UpdateQuestionsAction.class);
    
    public UpdateQuestionsAction(ActionExecutor actionExecutor, QuestionService questionService, PartitionService partitionService) {
        this.actionExecutor = actionExecutor;
        this.questionService = questionService;
        this.partitionService = partitionService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<Question> succeedQuestions = new ArrayList<>();
        Map<String,String> failedQuestionIds = new HashMap<>();
        Set<Partition> infectedPartitions = new HashSet<>();
        if (questions != null)
            for (Object questionObj : questions) {
                if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                    try {
                        String type = (String) questionDataMap.get("type");
                        Optional<Question> result;
                        switch (type) {
                            case "MultipleChoicesQuestion" -> {
                                result = actionExecutor.executeByTypeClass(CreateOrUpdateQuestion.class, questionDataMap);
                            }
                            case "QuestionGroup" -> {
                                result = actionExecutor.executeByTypeClass(CreateOrUpdateQuestionGroup.class, questionDataMap);
                            }
                            default -> throw new UnsupportedOperationException("type not supported: " + type);
                        }
                        if (result.isPresent()) {
                            Question question = result.get();
                            succeedQuestions.add(question);
                            final QuestionLinkImpl<?> linkWrapper = question.getLinkWrapper();
                            if (linkWrapper instanceof ToPartitionsLink link) {
                                infectedPartitions.addAll(link.getTargets());
                            }
                        } else {
                            logger.error("unknown error occurred at question of id: {}", questionDataMap.get("id").toString());
                            failedQuestionIds.put(questionDataMap.get("id").toString(), "unknown error");
                        }
                    } catch (PermissionDeniedException e) {
                        failedQuestionIds.put(questionDataMap.get("id").toString(), e.getMessage());
//                        throw e;
                    } catch (Exception e) {
                        failedQuestionIds.put(questionDataMap.get("id").toString(), e.getMessage());
                        logger.error("UpdateQuestionsAction.doAction", e);
//                        throw e;
                    }
                }
            }
        if (deletedQuestionIds != null) {
            List<String> succeedDeletedQuestionIds = new ArrayList<>();
            for (String deletedQuestionId : deletedQuestionIds) {
                Optional<Question> result = actionExecutor.executeByTypeClass(DeleteQuestionAction.class, deletedQuestionId);
                if (result.isPresent()) {
                    succeedDeletedQuestionIds.add(result.get().getId());
                    final QuestionLinkImpl<?> linkWrapper = result.get().getLinkWrapper();
                    if (linkWrapper instanceof ToPartitionsLink link) {
                        infectedPartitions.addAll(link.getTargets());
                    }
                } else {
                    succeedDeletedQuestionIds.add(deletedQuestionId);
                }
            }
            QuestionUpdateUtils.sendDeleteQuestionIdsToAll(succeedDeletedQuestionIds);
        }
        questionService.flush();
        QuestionUpdateUtils.sendUpdateQuestionsToAll(succeedQuestions);
        PartitionUpdateUtils.sendUpdatePartitionsToAll(infectedPartitions);
        LinkedHashMap<String, Object> map = getSuccessMap();
/*
        if (!succeedQuestions.isEmpty()) {
            map = getSuccessMap();
        } else {
            map = getErrorMap("none any question has been updated");
        }
*/
        map.put("succeedQuestionIds", succeedQuestions.stream().map(Question::getId).toList());
        map.put("failedQuestionReasons", failedQuestionIds);
        return Optional.of(map);
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