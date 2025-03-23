package indi.etern.checkIn.action.question.vote;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action("downVote")
public class DownVoteAction extends BaseAction1<DownVoteAction.Input, MessageOutput> {
    public record Input(String questionId) implements InputData {}
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        String questionId = context.getInput().questionId;
        Optional<Question> optionalQuestion = QuestionService.singletonInstance.findById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.getDownVoters().add(context.getCurrentUser());
            question.getUpVoters().remove(context.getCurrentUser());
            QuestionService.singletonInstance.update(question);
            context.resolve(MessageOutput.success("Down vote successful"));
        } else {
            context.resolve(MessageOutput.error("Question not exist"));
        }
    }
}