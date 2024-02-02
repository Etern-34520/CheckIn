package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class DeleteQuestionAction extends JsonResultAction {
    private final MultiPartitionableQuestion question;
    
    public DeleteQuestionAction(String questionID) {
        this.question = MultiPartitionableQuestionService.singletonInstance.getByMD5(questionID);
    }
    
    @Override
    public String requiredPermissionName() {
        final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (question == null) {
            return null;
        } else if (question.getAuthor().equals(currentUser)) {
            return "delete owns question";
        } else {
            return "delete others question";
        }
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        if (question != null) {
            MultiPartitionableQuestionService.singletonInstance.unbindAndDeleteById(question.getMd5());
        }
        final JsonObject res = new JsonObject();
        res.addProperty("type", "success");
        return Optional.of(res);
    }
    
    @Override
    public void afterAction() {
        if (question != null)
            sendDeleteQuestionToAll(question.getMd5());
    }
    
    private void sendDeleteQuestionToAll(String questionMD5) {
        JsonObject jsonObject = new JsonObject();
//        Map<String, Object> dataMap = new HashMap<>();
        jsonObject.addProperty("type", "deleteQuestion");
        jsonObject.addProperty("questionMD5", questionMD5);
//        dataMap.put("type", "deleteQuestion");
//        dataMap.put("questionMD5", questionMD5);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}
