package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.List;
import java.util.Optional;

public class BatchEnableOrDisableQuestionsAction extends QuestionAction {
    private final List<String> ids;
    private final User currentUser;
    private final boolean enable;
    private List<MultiPartitionableQuestion> multiPartitionableQuestions;

    public BatchEnableOrDisableQuestionsAction(List<String> ids, User currentUser, boolean enable) {
        this.ids = ids;
        this.currentUser = currentUser;
        this.enable = enable;
        this.multiPartitionableQuestions = MultiPartitionableQuestionService.singletonInstance.findAllById(ids);
    }

    @Override
    public String requiredPermissionName() {
        for (MultiPartitionableQuestion question : multiPartitionableQuestions) {
            if (question.getAuthor() == null) continue;
            if (!question.getAuthor().equals(currentUser)) {
                return "edit others question";
            }
        }
        return "create and edit owns question";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        if (enable) {
            multiPartitionableQuestions = MultiPartitionableQuestionService.singletonInstance.enableAllById(ids);
        } else {
            multiPartitionableQuestions = MultiPartitionableQuestionService.singletonInstance.disableAllById(ids);
        }
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
        sendUpdateQuestionsToAll(multiPartitionableQuestions);
    }
}
