package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action("getLikeQuestionsByUserQQ")
public class GetLikeQuestionsByUserQQAction extends BaseAction<GetLikeQuestionsByUserQQAction.Input,OutputData> {
    public record Input(long qq) implements InputData {}
    public record Output(List<Map<String,Object>> questions) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final UserService userService;
    private final QuestionService questionService;
    
    public GetLikeQuestionsByUserQQAction(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<GetLikeQuestionsByUserQQAction.Input, OutputData> context) {
        final Input input = context.getInput();
        Optional<User> optionalUser = userService.findByQQNumber(input.qq);
        if (optionalUser.isPresent()) {
            ArrayList<Map<String,Object>> questions = new ArrayList<>();
            questionService.findAllByUpVotersContains(optionalUser.get()).forEach(question -> {
                questions.add(QuestionUpdateUtils.getMapOfQuestion(question));
            });
            context.resolve(new GetDislikeQuestionsByUserQQAction.Output(questions));
        } else {
            context.resolve(MessageOutput.error("User not found"));
        }
    }
}
