package indi.etern.checkIn.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Map;
import java.util.Optional;

@Action(name = "getQuestionIdAndContentList")
public class GetQuestionIdAndContentListAction extends TransactionalAction {
    private int partitionId;
    private boolean shouldLogging = true;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    public boolean shouldLogging() {
        return shouldLogging;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Double) dataMap.get("partitionId")).intValue();
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(partitionId);
        if (optionalPartition.isPresent()) {
            JsonObject result = new JsonObject();
            JsonArray questionList = new JsonArray();
            result.add("questionList", questionList);
            for (var questionLink : optionalPartition.get().getQuestionLinks()) {
                Question question = questionLink.getSource();
                JsonObject questionInfo = new JsonObject();
                questionInfo.addProperty("id", question.getId());
                questionInfo.addProperty("content", question.getContent());
                questionInfo.addProperty("type", question.getClass().getSimpleName());
                questionList.add(questionInfo);
            }
            if (questionList.size() > 20) shouldLogging = false;
            return Optional.of(result);
        }
        return Optional.empty();
    }
}