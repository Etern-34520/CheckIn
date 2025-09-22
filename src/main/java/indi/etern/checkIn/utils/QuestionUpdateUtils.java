package indi.etern.checkIn.utils;

import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.dto.manage.question.BasicQuestionDTO;
import indi.etern.checkIn.dto.manage.question.ManageDTOUtils;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.ArrayList;
import java.util.List;

public class QuestionUpdateUtils {
    public static void sendUpdateQuestionsToAll(List<Question> questions) {
        List<BasicQuestionDTO> list = new ArrayList<>();
        for (Question question : questions) {
            list.add(ManageDTOUtils.ofQuestionBasic(question));
        }
        
        Message<?> message = Message.of("updateQuestions",list);
        WebSocketService.singletonInstance.sendMessageToAll(message);
    }
}
