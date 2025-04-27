package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.*;
import indi.etern.checkIn.action.question.delete.DeleteQuestionAction;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.web.WebSocketService;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import indi.etern.checkIn.utils.TransactionTemplateUtil;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Action("updateQuestions")
public class UpdateQuestionsAction extends BaseAction<UpdateQuestionsAction.Input, UpdateQuestionsAction.Output> {
    private final WebSocketService webSocketService;
    
    public record Input(@Nullable List<Map<String, Object>> updatedQuestions,
                        @Nullable List<String> deletedQuestionIds) implements InputData {
    }
    
    public record Output(OutputData.Result result,
                         List<String> succeedQuestionIds,
                         Map<String, String> failedQuestionIdReason) implements OutputData {
    }
    
    final ActionExecutor actionExecutor;
    final QuestionService questionService;
    final PartitionService partitionService;
    private final Logger logger = LoggerFactory.getLogger(UpdateQuestionsAction.class);
    
    public UpdateQuestionsAction(ActionExecutor actionExecutor, QuestionService questionService, PartitionService partitionService, WebSocketService webSocketService) {
        this.actionExecutor = actionExecutor;
        this.questionService = questionService;
        this.partitionService = partitionService;
        this.webSocketService = webSocketService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, Output> context) {
        final Input input = context.getInput();
        final List<Map<String, Object>> questions = input.updatedQuestions;
        final List<String> deletedQuestionIds = input.deletedQuestionIds;
        List<Question> succeedQuestions = new ArrayList<>();
        Map<String, String> failedQuestionIdReasons = new HashMap<>();
        Set<Partition> infectedPartitions = new HashSet<>();
        List<String> succeedDeletedQuestionIds = new ArrayList<>();
        TransactionTemplateUtil.getTransactionTemplate().executeWithoutResult((status) -> {
            if (questions != null)
                for (Object questionObj : questions) {
                    if (questionObj instanceof @SuppressWarnings("rawtypes")Map questionDataMap) {
                        try {
                            String type = (String) questionDataMap.get("type");
                            Question result;
                            switch (type) {
                                case "MultipleChoicesQuestion" -> {
                                    //noinspection unchecked
                                    ResultContext<CreateOrUpdateQuestion.SuccessOutput> resultContext =
                                            actionExecutor.execute(CreateOrUpdateQuestion.class,
                                                    new CreateOrUpdateQuestion.Input(questionDataMap));
                                    result = resultContext.getOutput().question();
                                }
                                case "QuestionGroup" -> {
                                    //noinspection unchecked
                                    ResultContext<CreateOrUpdateQuestionGroup.SuccessOutput> resultContext =
                                            actionExecutor.execute(CreateOrUpdateQuestionGroup.class,
                                                    new CreateOrUpdateQuestionGroup.Input(questionDataMap));
                                    result = resultContext.getOutput().questionGroup();
                                }
                                default -> throw new UnsupportedOperationException("type not supported: " + type);
                            }
                            if (result != null) {
                                succeedQuestions.add(result);
                                final QuestionLinkImpl<?> linkWrapper = result.getLinkWrapper();
                                if (linkWrapper instanceof ToPartitionsLink link) {
                                    infectedPartitions.addAll(link.getTargets());
                                }
                            } else {
                                logger.error("unknown error occurred at question of id: {}", questionDataMap.get("id").toString());
                                failedQuestionIdReasons.put(questionDataMap.get("id").toString(), "unknown error");
                            }
                        } catch (PermissionDeniedException e) {
                            failedQuestionIdReasons.put(questionDataMap.get("id").toString(), e.getMessage());
//                        throw e;
                        } catch (Exception e) {
                            failedQuestionIdReasons.put(questionDataMap.get("id").toString(), e.getMessage());
                            logger.error("When executing UpdateQuestionsAction: ", e);
                            if (logger.isTraceEnabled()) {
                                e.printStackTrace();
                            }
//                        throw e;
                        }
                    }
                }
            if (deletedQuestionIds != null) {
                for (String deletedQuestionId : deletedQuestionIds) {
                    try {
                        ResultContext<OutputData> context1 = actionExecutor.execute(DeleteQuestionAction.class, new DeleteQuestionAction.Input(deletedQuestionId));
                        final OutputData output = context1.getOutput();
                        if (output instanceof DeleteQuestionAction.Output(Question deletedQuestion)) {
                            succeedDeletedQuestionIds.add(deletedQuestion.getId());
                            final QuestionLinkImpl<?> linkWrapper = deletedQuestion.getLinkWrapper();
                            if (linkWrapper instanceof ToPartitionsLink link) {
                                infectedPartitions.addAll(link.getTargets());
                            }
                        } else {
                            if (output.result().equals(OutputData.Result.SUCCESS) || output.result().equals(OutputData.Result.WARNING)) {
                                succeedDeletedQuestionIds.add(deletedQuestionId);
                            } else {
                                if (output instanceof MessageOutput messageOutput) {
                                    failedQuestionIdReasons.put(deletedQuestionId, messageOutput.message());
                                } else {
                                    failedQuestionIdReasons.put(deletedQuestionId, "unknown error");
                                }
                            }
                        }
                    } catch (Exception e) {
                        failedQuestionIdReasons.put(deletedQuestionId, e.getMessage());
                        logger.error("When executing DeleteQuestionAction: ", e);
                        if (logger.isTraceEnabled()) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            status.flush();
        });
        
        if (deletedQuestionIds != null) {
            Message<?> message = Message.of("deleteQuestions", succeedDeletedQuestionIds);
            webSocketService.sendMessageToAll(message);
        }
        QuestionUpdateUtils.sendUpdateQuestionsToAll(succeedQuestions);
        
        Message<Collection<Partition>> message = Message.of("updatePartitions", infectedPartitions);
        webSocketService.sendMessageToAll(message);
        
        final List<String> succeedQuestionIds = succeedQuestions.stream().map(Question::getId).toList();
        OutputData.Result result;
        if ((succeedQuestionIds.isEmpty() && deletedQuestionIds != null && !failedQuestionIdReasons.isEmpty())) {
            result = OutputData.Result.ERROR;
        } else if (!failedQuestionIdReasons.isEmpty()) {
            result = OutputData.Result.WARNING;
        } else {
            result = OutputData.Result.SUCCESS;
        }
        context.resolve(new Output(result, succeedQuestionIds, failedQuestionIdReasons));
    }
}