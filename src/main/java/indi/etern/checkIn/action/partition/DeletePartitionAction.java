package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.utils.PartitionUpdateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Action("deletePartition")
public class DeletePartitionAction extends TransactionalAction {
    private Logger logger = LoggerFactory.getLogger(DeletePartitionAction.class);
    private int partitionId;
    final PartitionService partitionService;
    final QuestionService questionService;
    
    public DeletePartitionAction(PartitionService partitionService, QuestionService questionService) {
        this.partitionService = partitionService;
        this.questionService = questionService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "delete partition";
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Partition partition = partitionService.findById(partitionId).orElseThrow();
        List<Question> infectedquestions = new ArrayList<>();
        if (partition.getQuestionLinks().isEmpty()) {
            logger.debug("deleting empty partition \"" + partition.getName() + "\"");
            partitionService.delete(partition);
        } else {
            logger.debug("partition \"" + partition.getName() + "\" is not empty");
            for (ToPartitionsLink questionLink : partition.getQuestionLinks()) {
                final Set<Partition> partitions = questionLink.getTargets();
                partitions.remove(partition);
                final Question question = questionLink.getSource();
                infectedquestions.add(question);
                if (partitions.isEmpty()) {
                    logger.debug("deleting question (not beloned to other partitions) \"" + question.getId() + "\"");
                    questionService.delete(question);
                } else {
                    logger.debug("updating question \"" + question.getId() + "\"");
                    questionService.save(question);
                }
            }
            partitionService.delete(partition);
            QuestionUpdateUtils.sendUpdateQuestionsToAll(infectedquestions);
//            return getOptionalErrorMap("partition \"" + partition.getName() + "\" is not empty");
        }
        PartitionUpdateUtils.sendDeletePartitionToAll(partition);
        final LinkedHashMap<String, Object> successMap = getSuccessMap();
        if (!infectedquestions.isEmpty()) {
            successMap.put("infectedQuestionIds", infectedquestions.stream().map(Question::getId).toList());
        }
        return Optional.of(successMap);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Number) dataMap.get("id")).intValue();
    }
}
