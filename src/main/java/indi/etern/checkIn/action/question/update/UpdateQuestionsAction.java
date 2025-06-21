package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.*;
import indi.etern.checkIn.action.question.delete.DeleteQuestionAction;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.dto.manage.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.MultipleChoicesQuestionDTO;
import indi.etern.checkIn.dto.manage.QuestionGroupDTO;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
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
    final ActionExecutor actionExecutor;
    final QuestionService questionService;
    final PartitionService partitionService;
    private final WebSocketService webSocketService;
    private final Logger logger = LoggerFactory.getLogger(UpdateQuestionsAction.class);
    
    public UpdateQuestionsAction(ActionExecutor actionExecutor, QuestionService questionService, PartitionService partitionService, WebSocketService webSocketService) {
        this.actionExecutor = actionExecutor;
        this.questionService = questionService;
        this.partitionService = partitionService;
        this.webSocketService = webSocketService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, Output> context) {
        final Input input = context.getInput();
        final List<CommonQuestionDTO> questions = input.updatedQuestions;
        final List<String> deletedQuestionIds = input.deletedQuestionIds;
        List<Question> succeedQuestions = new ArrayList<>();
        Map<String, Collection<String>> failedQuestionIdReasons = new HashMap<>();
        Set<Partition> infectedPartitions = new HashSet<>();
        List<String> succeedDeletedQuestionIds = new ArrayList<>();
        TransactionTemplateUtil.getTransactionTemplate().executeWithoutResult((status) -> {
            if (questions != null)
                for (CommonQuestionDTO questionDTO : questions) {
                    try {
                        Question result = null;
                        if (questionDTO instanceof MultipleChoicesQuestionDTO multipleChoicesQuestionDTO) {
                            ResultContext<OutputData> resultContext =
                                    actionExecutor.execute(CreateOrUpdateMultipleChoicesQuestion.class,
                                            new CreateOrUpdateMultipleChoicesQuestion.Input(multipleChoicesQuestionDTO));
                            final OutputData output = resultContext.getOutput();
                            if (output instanceof CreateOrUpdateMultipleChoicesQuestion.SuccessOutput(Question question)) {
                                result = question;
                            } else if (output instanceof CreateOrUpdateMultipleChoicesQuestion.ErrorOutput(Collection<String> messages)){
                                failedQuestionIdReasons.put(questionDTO.getId(), messages);
                            } else {
                                throw new IllegalStateException("Unexpected value: " + output);
                            }
                        } else if (questionDTO instanceof QuestionGroupDTO questionGroupDTO) {
                            ResultContext<OutputData> resultContext =
                                    actionExecutor.execute(CreateOrUpdateQuestionGroup.class,
                                            new CreateOrUpdateQuestionGroup.Input(questionGroupDTO));
                            final OutputData output = resultContext.getOutput();
                            if (output instanceof CreateOrUpdateQuestionGroup.SuccessOutput(QuestionGroup questionGroup)) {
                                result = questionGroup;
                            } else if (output instanceof CreateOrUpdateQuestionGroup.ErrorOutput(Collection<String> messages)){
                                failedQuestionIdReasons.put(questionDTO.getId(), messages);
                            } else {
                                throw new IllegalStateException("Unexpected value: " + output);
                            }
                        }
                        
                        if (result != null) {
                            succeedQuestions.add(result);
                            final QuestionLinkImpl<?> linkWrapper = result.getLinkWrapper();
                            if (linkWrapper instanceof ToPartitionsLink link) {
                                infectedPartitions.addAll(link.getTargets());
                            }
                        } /*else {
                            logger.error("unknown error occurred at question of id: {}", questionDTO.getId());
                            failedQuestionIdReasons.put(questionDTO.getId(), Collections.singleton("unknown error"));
                        }
*/                    } catch (PermissionDeniedException e) {
                        failedQuestionIdReasons.put(questionDTO.getId(), Collections.singleton(e.getMessage()));
//                        throw e;
                    } catch (Exception e) {
                        failedQuestionIdReasons.put(questionDTO.getId(), Collections.singleton(e.getMessage()));
                        logger.error("When executing UpdateQuestionsAction: {} : {}", e.getClass().getSimpleName(), e.getMessage());
                        if (logger.isDebugEnabled()) {
                            e.printStackTrace();
                        }
//                        throw e;
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
                                    failedQuestionIdReasons.put(deletedQuestionId, Collections.singleton(messageOutput.message()));
                                } else {
                                    failedQuestionIdReasons.put(deletedQuestionId, Collections.singleton("unknown error"));
                                }
                            }
                        }
                    } catch (Exception e) {
                        failedQuestionIdReasons.put(deletedQuestionId, Collections.singleton(e.getMessage()));
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
        
        final List<String> succeedUpdatedQuestionIds = succeedQuestions.stream().map(Question::getId).toList();
        OutputData.Result result;
        if (succeedUpdatedQuestionIds.isEmpty() && !failedQuestionIdReasons.isEmpty()) {
            result = OutputData.Result.ERROR;
        } else if (!failedQuestionIdReasons.isEmpty()) {
            result = OutputData.Result.WARNING;
        } else {
            result = OutputData.Result.SUCCESS;
        }
        context.resolve(new Output(result, succeedUpdatedQuestionIds, succeedDeletedQuestionIds, failedQuestionIdReasons));
    }
    
    public record Input(@Nullable List<CommonQuestionDTO> updatedQuestions,
                        @Nullable List<String> deletedQuestionIds) implements InputData {
    }
    
    public record Output(Result result,
                         List<String> succeedUpdatedQuestionIds,
                         List<String> succeedDeletedQuestionIds,
                         Map<String, Collection<String>> failedQuestionIdReason) implements OutputData {
    }
}