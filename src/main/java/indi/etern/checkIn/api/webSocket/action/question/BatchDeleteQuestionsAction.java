package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.List;
import java.util.Optional;

import static indi.etern.checkIn.api.webSocket.action.question.utils.Utils.sendDeleteQuestionToAll;

public class BatchDeleteQuestionsAction extends TransactionalAction {
    List<String> ids;
    User currentUser;

    public BatchDeleteQuestionsAction(List<String> ids, User currentUser) {
        this.ids = ids;
        this.currentUser = currentUser;
    }

    @Override
    public String requiredPermissionName() {
        return "delete others question";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        MultiPartitionableQuestionService.singletonInstance.deleteAllById(ids);
        return successOptionalJsonObject;
    }

    @Override
    public void afterAction() {
        for (String id : ids) {
            sendDeleteQuestionToAll(id);
        }
    }
}
