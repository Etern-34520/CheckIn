package indi.etern.checkIn.action.question;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.utils.QuestionUpdateUtils;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getLikeQuestionsByUserQQ")
public class GetLikeQuestionsByUserQQAction extends TransactionalAction {
    private long userQQ;
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;
    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<User> optionalUser = userService.findByQQNumber(userQQ);
        LinkedHashMap<String,Object> result;
        if (optionalUser.isPresent()) {
            result = new LinkedHashMap<>();
            ArrayList<Object> questions = new ArrayList<>();
            questionService.findAllByUpVotersContains(optionalUser.get()).forEach(question -> {
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
