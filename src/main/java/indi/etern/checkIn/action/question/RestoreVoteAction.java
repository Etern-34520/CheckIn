package indi.etern.checkIn.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;

import javax.management.InstanceNotFoundException;
import java.util.Map;
import java.util.Optional;

@Action(name = "restoreVote")
public class RestoreVoteAction extends TransactionalAction {
    private String questionId;

    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        User currentUser = getCurrentUser();
        Optional<Question> optionalQuestion = QuestionService.singletonInstance.findById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.getUpVoters().remove(currentUser);
            question.getDownVoters().remove(currentUser);
            QuestionService.singletonInstance.update(question);
        } else {
            throw new InstanceNotFoundException("question: "+questionId);
        }
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        questionId = (String) dataMap.get("questionId");
    }
}
