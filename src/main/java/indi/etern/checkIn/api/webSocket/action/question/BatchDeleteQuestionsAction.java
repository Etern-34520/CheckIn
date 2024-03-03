package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.List;
import java.util.Optional;

public class BatchDeleteQuestionsAction extends QuestionAction {
    List<String> md5s;
    User currentUser;

    public BatchDeleteQuestionsAction(List<String> md5s, User currentUser) {
        this.md5s = md5s;
        this.currentUser = currentUser;
    }

    @Override
    public String requiredPermissionName() {
        return "delete others question";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        MultiPartitionableQuestionService.singletonInstance.deleteAllById(md5s);
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
        for (String md5 : md5s) {
            sendDeleteQuestionToAll(md5);
        }
    }
}
