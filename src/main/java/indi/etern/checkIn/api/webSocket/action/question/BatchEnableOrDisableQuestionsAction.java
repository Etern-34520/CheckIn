package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.List;
import java.util.Optional;

public class BatchEnableOrDisableQuestionsAction extends QuestionAction {
    private final List<String> md5s;
    private final User currentUser;
    private final boolean enable;
    private List<MultiPartitionableQuestion> multiPartitionableQuestions;

    public BatchEnableOrDisableQuestionsAction(List<String> md5s, User currentUser, boolean enable) {
        this.md5s = md5s;
        this.currentUser = currentUser;
        this.enable = enable;
        this.multiPartitionableQuestions = MultiPartitionableQuestionService.singletonInstance.findAllById(md5s);
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
            multiPartitionableQuestions = MultiPartitionableQuestionService.singletonInstance.enableAllById(md5s);
        } else {
            multiPartitionableQuestions = MultiPartitionableQuestionService.singletonInstance.disableAllById(md5s);
        }
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
        sendUpdateQuestionsToAll(multiPartitionableQuestions);
    }
}
