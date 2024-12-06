/*
package indi.etern.checkIn.service.exam;

@Service
public class ExamCheckService {
    private final ObjectMapper objectMapper;
    private final TrafficRecordService userTrafficService;
    private final QuestionService multiPartitionableQuestionService;
    private SettingService settingService;
    
    protected ExamCheckService(ObjectMapper objectMapper,
                               TrafficRecordService userTrafficService,
                               QuestionService multiPartitionableQuestionService,
                               SettingService settingService) {
        this.objectMapper = objectMapper;
        this.userTrafficService = userTrafficService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.settingService = settingService;
    }
    
    public ExamResult check(long qq, Map<String, Object> data) {//TODO
        final Optional<UserTraffic> lastTraffic = userTrafficService.findLastByInfoContainsEntryOf(qq, "action", "generateExam");
        if (lastTraffic.isEmpty()) throw new ExamException("No exam generated.");
        return new ExamResult(qq,0,0,0,"",false);
    }
}
*/
