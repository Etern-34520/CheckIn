package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getQuestionSimpleData")
public class GetQuestionsSimpleDataAction extends TransactionalAction {
    private int partitionId;
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Number) dataMap.get("partitionId")).intValue();
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(partitionId);
        if (optionalPartition.isPresent()) {
            LinkedHashMap<String, Object> result = getSuccessMap();
            ArrayList<Object> questionList = new ArrayList<>();
            result.put("questionList", questionList);
            for (var questionLink : optionalPartition.get().getQuestionLinks()) {
                Question question = questionLink.getSource();
                LinkedHashMap<String, Object> questionInfo = new LinkedHashMap<>();
                questionInfo.put("id", question.getId());
                questionInfo.put("content", question.getContent());
                final User author = question.getAuthor();
                if (author != null)
                    questionInfo.put("authorQQ", author.getQQNumber());
                questionInfo.put("type", question.getClass().getSimpleName());
                questionList.add(questionInfo);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }
}