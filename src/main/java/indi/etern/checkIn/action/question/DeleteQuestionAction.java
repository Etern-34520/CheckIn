package indi.etern.checkIn.action.question;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Action(value = "deleteQuestion", exposed = false)
public class DeleteQuestionAction extends BaseAction<Question, String> {
    private final QuestionService questionService;
    private String questionId;
    private Question question;
    
    protected DeleteQuestionAction(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public String requiredPermissionName() {
        final User currentUser = getCurrentUser();
        if (question == null) {
            return "";
        } if (currentUser.equals(question.getAuthor())) {
            return "delete owns question";
        } else {
            return "delete others question";
        }
    }

    @Override
    protected Optional<Question> doAction() throws Exception {
        if (question != null) {
            questionService.delete(question);
            return Optional.of(question);
        } else {
            return Optional.empty();
        }
    }
    
    @SneakyThrows
    @Override
    @Transactional
    public Optional<Question> call() {
        return super.call();
    }

    @Override
    public void initData(String questionId) {
        question = questionService.findById(questionId).orElse(null);
        this.questionId = questionId;
    }
    
}