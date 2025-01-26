package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.update.utils.Utils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Action(value = "createOrUpdateQuestion", exposed = false)
public class CreateOrUpdateQuestion extends BaseAction<Question, Map<?, ?>> {
    final QuestionService multiPartitionableQuestionService;
    private final PartitionService partitionService;
    private final ActionExecutor actionExecutor;
    
    private Map<?, ?> questionDataMap;
    private Question multiPartitionableQuestion = null;
    
    public CreateOrUpdateQuestion(QuestionService multiPartitionableQuestionService, PartitionService partitionService, PartitionService partitionService1, ActionExecutor actionExecutor) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.partitionService = partitionService1;
        this.actionExecutor = actionExecutor;
    }
    
    @Override
    public String requiredPermissionName() {
        Set<String> requiredPermissionNames = new HashSet<>();
        Optional<Question> previousQuestion = multiPartitionableQuestionService.findById(questionDataMap.get("id").toString());
        multiPartitionableQuestion = Utils.createMultipleChoicesQuestion(questionDataMap);
        if (questionDataMap.containsKey("authorQQ")) {
            requiredPermissionNames.add("change author");
        }
        if (previousQuestion.isPresent() &&
                multiPartitionableQuestion.getAuthor() != null && previousQuestion.get().getAuthor() != null &&
                multiPartitionableQuestion.getAuthor().getQQNumber() == previousQuestion.get().getAuthor().getQQNumber()) {
            requiredPermissionNames.add("create and edit owns question");
        } else {
            requiredPermissionNames.add("edit others question");
        }
        if (questionDataMap.containsKey("enabled")) {
            requiredPermissionNames.add("enable and disable question");
        }
        return String.join(",", requiredPermissionNames);
    }
    
    @Override
    protected Optional<Question> doAction() throws Exception {
        multiPartitionableQuestionService.save(multiPartitionableQuestion);
        return Optional.of(multiPartitionableQuestion);
    }
    
    @Override
    public void initData(Map<?, ?> questionMap) {
        this.questionDataMap = questionMap;
    }
}