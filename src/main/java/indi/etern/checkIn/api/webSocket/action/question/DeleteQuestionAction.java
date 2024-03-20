package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class DeleteQuestionAction extends QuestionAction {
    private MultiPartitionableQuestion question;
    
    public DeleteQuestionAction(String questionID) {
        this.question = MultiPartitionableQuestionService.singletonInstance.getById(questionID);
    }

    protected DeleteQuestionAction() {}
    
    @Override
    public String requiredPermissionName() {
        final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (question == null) {
            return null;
        } else if (currentUser.equals(question.getAuthor())) {
            return "delete owns question";
        } else {
            return "delete others question";
        }
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        if (question != null) {
            MultiPartitionableQuestionService.singletonInstance.unbindAndDeleteById(question.getId());
        }
        return successOptionalJsonObject;
    }
    
    @Override
    public void afterAction() {
        if (question != null)
            sendDeleteQuestionToAll(question.getId());
    }

}
