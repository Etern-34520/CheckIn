package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.utils.PartitionUpdateUtils;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Action("deletePartition")
public class DeletePartitionAction extends BaseAction<DeletePartitionAction.Input, OutputData> {
    public record Input(@Nonnull String partitionId) implements InputData {
    }
    
    public record SuccessOutput(List<String> infectedQuestionIds) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final Logger logger = LoggerFactory.getLogger(DeletePartitionAction.class);
    final PartitionService partitionService;
    final QuestionService questionService;
    
    public DeletePartitionAction(PartitionService partitionService, QuestionService questionService) {
        this.partitionService = partitionService;
        this.questionService = questionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        context.requirePermission("delete partition");
        
        final Input input = context.getInput();
        Optional<Partition> optionalPartition = partitionService.findById(input.partitionId);
        if (optionalPartition.isPresent()) {
            List<Question> infectedQuestions = new ArrayList<>();
            final Partition partition = optionalPartition.get();
            if (partition.getQuestionLinks().isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.debug("deleting empty partition \"{}\"", partition.getName());
                partitionService.delete(optionalPartition.orElse(null));
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("partition \"{}\" is not empty", partition.getName());
                for (ToPartitionsLink questionLink : partition.getQuestionLinks()) {
                    final Set<Partition> partitions = questionLink.getTargets();
                    partitions.remove(partition);
                    final Question question = questionLink.getSource();
                    infectedQuestions.add(question);
                    if (partitions.isEmpty()) {
                        if (logger.isDebugEnabled())
                            logger.debug("deleting question (not belonged to other partitions) \"{}\"", question.getId());
                        questionService.delete(question);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("updating question \"{}\"", question.getId());
                        questionService.save(question);
                    }
                }
                partitionService.delete(optionalPartition.orElse(null));
                QuestionUpdateUtils.sendUpdateQuestionsToAll(infectedQuestions);
//            return getOptionalErrorMap("partition \"" + partition.getName() + "\" is not empty");
            }
            PartitionUpdateUtils.sendDeletePartitionToAll(optionalPartition.orElse(null));
            if (infectedQuestions.isEmpty()) {
                context.resolve(new SuccessOutput(null));
            } else {
                context.resolve(new SuccessOutput(infectedQuestions.stream().map(Question::getId).toList()));
            }
        } else {
            context.resolve(MessageOutput.error("Partition not exist"));
        }
    }
}
