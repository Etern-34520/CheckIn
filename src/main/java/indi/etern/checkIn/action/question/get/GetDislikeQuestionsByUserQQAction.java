package indi.etern.checkIn.action.question.get;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getDislikeQuestionsByUserQQ")
public class GetDislikeQuestionsByUserQQAction extends TransactionalAction {
    private long userQQ;
    private final UserService userService;
    private final QuestionService questionService;
    
    public GetDislikeQuestionsByUserQQAction(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<User> optionalUser = userService.findByQQNumber(userQQ);
        LinkedHashMap<String,Object> result;
        if (optionalUser.isPresent()) {
            result = getSuccessMap();
            ArrayList<Object> questions = new ArrayList<>();
            questionService.findAllByDownVotersContains(optionalUser.get()).forEach(question -> {
                questions.add(QuestionUpdateUtils.getMapOfQuestion(question));
            });
            result.put("questions", questions);
        } else {
            result = getErrorMap("user not exist");
        }
        return Optional.of(result);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        userQQ = ((Number) dataMap.get("qq")).longValue();
    }
}
