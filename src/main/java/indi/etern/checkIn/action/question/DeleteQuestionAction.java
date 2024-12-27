package indi.etern.checkIn.action.question;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;

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
        if (currentUser.equals(question.getAuthor())) {
            return "delete owns question";
        } else {
            return "delete others question";
        }
    }

    @Override
    protected Optional<Question> doAction() throws Exception {
        questionService.delete(question);
        return Optional.of(question);
    }

    @Override
    public void initData(String questionId) {
        question = questionService.findById(questionId).orElseThrow();
        this.questionId = questionId;
    }
    
}