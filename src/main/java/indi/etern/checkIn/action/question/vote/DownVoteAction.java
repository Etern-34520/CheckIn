package indi.etern.checkIn.action.question.vote;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import javax.management.InstanceNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("downVote")
public class DownVoteAction extends TransactionalAction {
    private String questionId;
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<Question> optionalQuestion = QuestionService.singletonInstance.findById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.getDownVoters().add(getCurrentUser());
            question.getUpVoters().remove(getCurrentUser());
            QuestionService.singletonInstance.save(question);
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
