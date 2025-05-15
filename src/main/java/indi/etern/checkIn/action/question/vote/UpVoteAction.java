package indi.etern.checkIn.action.question.vote;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action("upVote")
public class UpVoteAction extends BaseAction<UpVoteAction.Input, MessageOutput> {
    private final QuestionService questionService;
    
    public UpVoteAction(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    public record Input(String questionId) implements InputData {}
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        String questionId = context.getInput().questionId;
        User currentUser = context.getCurrentUser();
        Optional<Question> optionalQuestion = QuestionService.singletonInstance.findById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.getUpVoters().add(currentUser);
            question.getDownVoters().remove(currentUser);
            questionService.save(question);
            context.resolve(MessageOutput.success("Down vote successful"));
        } else {
            context.resolve(MessageOutput.error("Question not exist"));
        }
    }
}