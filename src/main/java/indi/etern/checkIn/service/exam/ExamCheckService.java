package indi.etern.checkIn.service.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import org.springframework.stereotype.Service;

@Service
public class ExamCheckService {
    private final ObjectMapper objectMapper;
    private final UserTrafficService userTrafficService;
    private final MultiPartitionableQuestionService multiPartitionableQuestionService;
    private SettingService settingService;
    
    protected ExamCheckService(ObjectMapper objectMapper,
                               UserTrafficService userTrafficService,
                               MultiPartitionableQuestionService multiPartitionableQuestionService,
                               SettingService settingService) {
        this.objectMapper = objectMapper;
        this.userTrafficService = userTrafficService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.settingService = settingService;
    }
    
    /*public ExamResult check(long qq, Map<String, Object> data) {
        final Optional<UserTraffic> lastTraffic = userTrafficService.findLastByInfoContainsEntryOf(qq, "action", "generateExam");
        if (lastTraffic.isEmpty()) throw new ExamException("No exam generated.");
        List<String> questionIds;
        try {
            //noinspection unchecked
            questionIds = objectMapper.readValue(lastTraffic.get().getAttributesMap().get("examQuestionIds"), List.class);
            Map<String, MultiPartitionableQuestion> questionsMap = multiPartitionableQuestionService.mapAllById(questionIds);
            int correctCount = 0;
            int questionCount = questionIds.size();
            for (String questionId : questionIds) {
                if (!data.containsKey(questionId)) {
                    if (multiPartitionableQuestionService.existsById(questionId)) {
                        throw new ExamException("Missing question: " + questionId);
                    } else {
                        questionCount--;
                    }
                }
                else {
                    ExamResultQuestion examResultQuestion = objectMapper.convertValue(data.get(questionId), ExamResultQuestion.class);
                    if (examResultQuestion.checkWith((MultipleChoiceQuestion) questionsMap.get(questionId))) {
                        correctCount++;
                    }
                }
            }
            final int score = correctCount * 100 / questionCount;
            boolean passed = score >= Integer.parseInt(settingService.get("checking.passScore"));
            ExamResult examResult = new ExamResult(qq, score, correctCount, questionCount,
                    passed ? settingService.get("checking.passMessage") : settingService.get("checking.notPassMessage"), passed);
            examResult.setQuestionIds(questionIds);
            return examResult;
        } catch (JsonProcessingException e) {
            throw new ExamException("Error while parsing exam data.");
        }
    }*/
}
