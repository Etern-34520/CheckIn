package indi.etern.checkIn.action.question;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.Optional;

@Action(name = "deleteQuestion")
public class DeleteQuestionAction extends BaseAction<String, String> {
    private String questionId;

    protected DeleteQuestionAction() {
    }

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
    protected Optional<String> doAction() throws Exception {
        QuestionService.singletonInstance.deleteById(questionId);
        return Optional.of(questionId);
    }

    @Override
    public void initData(String questionId) {
        //noinspection unchecked
        this.questionId = questionId;
    }
}
