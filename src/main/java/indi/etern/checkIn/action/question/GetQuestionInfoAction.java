package indi.etern.checkIn.action.question;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.utils.Utils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getQuestionInfo")
public class GetQuestionInfoAction extends TransactionalAction {
    private String questionId;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(questionId);
        if (questionOptional.isEmpty()) {
            LinkedHashMap<String,Object> notFound = new LinkedHashMap<>();
            notFound.put("type", "question not found");
            return Optional.of(notFound);
        } else {
            return Optional.of(Utils.getMapOfQuestion(questionOptional.get()));
        }
    }

    /*@Override
    public Optional<LinkedHashMap<String,Object>> getLogMessage(Optional<LinkedHashMap<String,Object>> result) {
*//*
        //noinspection OptionalGetWithoutIsPresent Impossible to be null
        LinkedHashMap<String,Object> jsonObject = result.get();
        JsonElement images1 = jsonObject.get("images");
        if (images1 instanceof ArrayList<Object> images) {
            for (JsonElement image : images) {
                LinkedHashMap<String,Object> imageObject = (LinkedHashMap<String,Object>) image;
//                imageObject.remove("url");
//                imageObject.put("url", "[ image base64 url (masked due to length) ]");
            }
        }
        return Optional.of(jsonObject);
*//*
        return result;
    }*/

    @Override
    public void initData(Map<String, Object> dataMap) {
        questionId = (String) dataMap.get("questionId");
    }
}
