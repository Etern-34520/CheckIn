package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;

import javax.management.InstanceNotFoundException;
import java.util.Optional;

public class UpVoteAction extends TransactionalAction {
    private final String questionId;
    private final User currentUser;
    
    public UpVoteAction(String questionId, User currentUser) {
        this.questionId = questionId;
        this.currentUser = currentUser;
    }
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<MultiPartitionableQuestion> optionalMultiPartitionableQuestion = MultiPartitionableQuestionService.singletonInstance.findById(questionId);
        if (optionalMultiPartitionableQuestion.isPresent()) {
            MultiPartitionableQuestion question = optionalMultiPartitionableQuestion.get();
            question.getUpVoters().add(currentUser);
            question.getDownVoters().remove(currentUser);
            MultiPartitionableQuestionService.singletonInstance.update(question);
        } else {
            throw new InstanceNotFoundException("question"+questionId);
        }
        return Optional.empty();
    }
}
