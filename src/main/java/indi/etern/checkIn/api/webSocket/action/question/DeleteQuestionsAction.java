package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import java.util.List;
import java.util.Optional;

import static indi.etern.checkIn.api.webSocket.action.question.utils.Utils.sendDeleteQuestionsToAll;

public class DeleteQuestionsAction extends TransactionalAction {
    private List<String> questionIds;
    
    public DeleteQuestionsAction(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    protected DeleteQuestionsAction() {}
    
    @Override
    public String requiredPermissionName() {
//        final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return null;
/*
        if (question == null) {
            return null;
        } else if (currentUser.equals(question.getAuthor())) {
            return "delete owns question";
        } else {
            return "delete others question";
        }
*/
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        if (questionIds != null) {
            MultiPartitionableQuestionService.singletonInstance.deleteAllById(questionIds);
        }
        return successOptionalJsonObject;
    }
    
    @Override
    public void afterAction() {
        if (questionIds != null)
            sendDeleteQuestionsToAll(questionIds);
    }

}
