package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Optional;

public class GetQuestionIdAndContentListAction extends QuestionAction{
    private final int partitionId;
    private boolean shouldLogging = true;

    public GetQuestionIdAndContentListAction(int partitionId) {
        this.partitionId = partitionId;
    }

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    public boolean shouldLogging() {
        return shouldLogging;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(partitionId);
        if (optionalPartition.isPresent()) {
            JsonObject result = new JsonObject();
            JsonArray questionList = new JsonArray();
            result.add("questionList", questionList);
            for (var question : optionalPartition.get().getQuestions()) {
                JsonObject questionInfo = new JsonObject();
                questionInfo.addProperty("id", question.getId());
                questionInfo.addProperty("content", question.getContent());
                questionList.add(questionInfo);
            }
            if (questionList.size() > 20) shouldLogging = false;
            return Optional.of(result);
        }
        return Optional.empty();
    }
}