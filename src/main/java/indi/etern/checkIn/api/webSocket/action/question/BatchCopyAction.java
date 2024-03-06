package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BatchCopyAction extends QuestionAction {
    private final List<String> questionIds;
    private final List<Integer> targetPartitionsId;
    private final List<MultiPartitionableQuestion> questions;

    public BatchCopyAction(List<String> questionIds, List<Integer> targetPartitionsId) {
        this.questionIds = questionIds;
        this.targetPartitionsId = targetPartitionsId;
        this.questions = new ArrayList<>(questionIds.size());
    }

    @Override
    public String requiredPermissionName() {
        return "edit others question";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        for (String questionId : questionIds) {
            MultiPartitionableQuestion multiPartitionableQuestion = MultiPartitionableQuestionService.singletonInstance.findById(questionId).orElseThrow();
            for (Integer targetPartitionId : targetPartitionsId) {
                multiPartitionableQuestion.getPartitions().add(Partition.getInstance(targetPartitionId));
            }
            MultiPartitionableQuestionService.singletonInstance.save(multiPartitionableQuestion);
            this.questions.add(multiPartitionableQuestion);
        }
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
//        sendUpdateQuestionsToAll(questions);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "batchCopy");
        JsonArray jsonArray = new JsonArray();
        for (int targetPartitionId : targetPartitionsId) {
            jsonArray.add(targetPartitionId);
        }
        jsonObject.add("partitionIds", jsonArray);
        JsonArray jsonArray1 = new JsonArray();
        for (MultiPartitionableQuestion question:questions) {
            jsonArray1.add(question.getMd5());
        }
        jsonObject.add("questionIds", jsonArray1);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}
