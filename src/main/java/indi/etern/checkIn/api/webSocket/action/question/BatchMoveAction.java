package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BatchMoveAction extends QuestionAction {
    private final List<String> questionIds;
    private final List<Integer> targetPartitionsId;
    private final List<MultiPartitionableQuestion> questions;

    public BatchMoveAction(List<String> questionIds, List<Integer> targetPartitionsId) {
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
            MultipleQuestionBuilder questionBuilder = MultipleQuestionBuilder.from((MultipleChoiceQuestion) multiPartitionableQuestion);
            questionBuilder.getPartitions().clear();
            questionBuilder.addPartitionsById(targetPartitionsId);
            MultiPartitionableQuestionService.singletonInstance.deleteById(multiPartitionableQuestion.getMd5());
            multiPartitionableQuestion = questionBuilder.build();
            MultiPartitionableQuestionService.singletonInstance.update(multiPartitionableQuestion);
            this.questions.add(multiPartitionableQuestion);
        }
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
        sendUpdateQuestionsToAll(questions);
    }
}
