package indi.etern.checkIn.action.question.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.update.utils.Utils;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

@Action("createOrUpdateQuestionGroup")
public class CreateOrUpdateQuestionGroup extends BaseAction<Object, Map<String,Object>> {
    private final ObjectMapper objectMapper;
    Map<String,Object> data;
    QuestionGroup questionGroup;
    final QuestionService multiPartitionableQuestionService;
    private final Logger logger = LoggerFactory.getLogger(CreateOrUpdateQuestionGroup.class);

    public CreateOrUpdateQuestionGroup(QuestionService multiPartitionableQuestionService, @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String requiredPermissionName() {
        //TODO
        return "";
    }

    @Override
    protected Optional<Object> doAction() throws Exception {
        multiPartitionableQuestionService.save(questionGroup);
        return Optional.of(questionGroup);
    }

    @Override
    public void initData(Map<String,Object> dataMap) {
        data = dataMap;
        questionGroup = Utils.createQuestionGroup(data);
    }

    @Override
    protected void preLog() {
        try {
            Map<String,Object> copy = new HashMap<>(data);
            //noinspection unchecked
            List<Map<String,Object>> images = (List<Map<String,Object>>) copy.getOrDefault  ("images",List.of());
            List<Map<String,Object>> images1 = new ArrayList<>();
            for (Map<String,Object> i:images) {
                HashMap<String, Object> e = new HashMap<>(i);
                e.put("url","[ masked due to length ]");
                images1.add(e);
            }
            copy.put("images",images1);
            logger.info(objectMapper.writeValueAsString(copy));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
