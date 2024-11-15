package indi.etern.checkIn.action.question.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

import static indi.etern.checkIn.action.question.update.utils.Utils.createMultipleChoicesQuestion;

@Action("createOrUpdateQuestion")
public class CreateOrUpdateQuestionAction extends BaseAction<Question, Map<?, ?>> {
    final QuestionService multiPartitionableQuestionService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(CreateOrUpdateQuestionAction.class);
    
    private Map<?, ?> questionDataMap;
    private Question multiPartitionableQuestion = null;
    
    public CreateOrUpdateQuestionAction(QuestionService multiPartitionableQuestionService, @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String requiredPermissionName() {
        Set<String> requiredPermissionNames = new HashSet<>();
        Optional<Question> previousQuestion = multiPartitionableQuestionService.findById(questionDataMap.get("id").toString());
        multiPartitionableQuestion = createMultipleChoicesQuestion(questionDataMap);
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
    
    @Override
    protected void preLog() {
        try {
            //noinspection unchecked
            Map<String, Object> copy = (Map<String, Object>) new HashMap<>(questionDataMap);
            //noinspection unchecked
            List<Map<String, Object>> images = (List<Map<String, Object>>) copy.getOrDefault("images", List.of());
            List<Map<String, Object>> images1 = new ArrayList<>();
            for (Map<String, Object> i : images) {
                HashMap<String, Object> e = new HashMap<>(i);
                e.put("url", "[ masked due to length ]");
                images1.add(e);
            }
            copy.put("images", images1);
            logger.info(objectMapper.writeValueAsString(copy));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
