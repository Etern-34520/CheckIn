package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.QuestionCreateUtils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Action(value = "createOrUpdateQuestion", exposed = false)
public class CreateOrUpdateQuestion extends BaseAction<Question, Map<?, ?>> {
    final QuestionService questionService;
    
    private Map<?, ?> questionDataMap;
    private Question multiPartitionableQuestion = null;
    
    public CreateOrUpdateQuestion(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @Override
    public String requiredPermissionName() {
        Set<String> requiredPermissionNames = new HashSet<>();
        Optional<Question> previousQuestion = questionService.findById(questionDataMap.get("id").toString());
        multiPartitionableQuestion = QuestionCreateUtils.createMultipleChoicesQuestion(questionDataMap);
        if (previousQuestion.isPresent() && questionDataMap.containsKey("authorQQ")) {
            requiredPermissionNames.add("change question author");
        }
        if (previousQuestion.isEmpty() ||
                previousQuestion.get().getAuthor() != null &&
                previousQuestion.get().getAuthor().equals(getCurrentUser())) {
            requiredPermissionNames.add("create and edit owns questions");
        } else {
            requiredPermissionNames.add("edit others questions");
        }
        if (questionDataMap.containsKey("enabled")) {
            requiredPermissionNames.add("enable and disable questions");
        }
        return String.join(",", requiredPermissionNames);
    }
    
    @Override
    protected Optional<Question> doAction() throws Exception {
        questionService.save(multiPartitionableQuestion);
        return Optional.of(multiPartitionableQuestion);
    }
    
    @Override
    public void initData(Map<?, ?> questionMap) {
        this.questionDataMap = questionMap;
    }
}