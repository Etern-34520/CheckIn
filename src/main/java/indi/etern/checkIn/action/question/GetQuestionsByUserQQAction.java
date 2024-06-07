package indi.etern.checkIn.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.utils.Utils;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@Action(name = "getQuestionsByUserQQ")
public class GetQuestionsByUserQQAction extends TransactionalAction {
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
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<User> optionalUser = userService.findByQQNumber(userQQ);
        JsonObject result;
        if (optionalUser.isPresent()) {
            result = new JsonObject();
            JsonArray questions = new JsonArray();
            questionService.findAllByAuthor(optionalUser.get()).forEach(question -> {
                questions.add(Utils.getJsonObjectOf(question));
            });
            result.add("questions", questions);
        } else {
            result = getErrorJsonObject("user not exist");
        }
        return Optional.of(result);
    }

    @Override
    public void initData(Map<String, Object> dataObj) {
        userQQ = ((Double) dataObj.get("qq")).longValue();
    }
}
