package indi.etern.checkIn.controller.rest;

//@RestController
public class CheckApi {
/*
    private final UserTrafficService userTrafficService;
    private final ObjectMapper objectMapper;
    private final SettingService settingService;
    private final UserService userService;
    private final String token;

    public CheckApi(UserTrafficService userTrafficService, ObjectMapper objectMapper, SettingService settingService, UserService userService) {
        this.userTrafficService = userTrafficService;
        this.objectMapper = objectMapper;
        this.settingService = settingService;
        this.userService = userService;
        this.token = settingService.get("other.robotToken");
        if (token==null) throw new IllegalStateException("robot token is null");
    }

    @RequestMapping("/api/check/")
    public ExamResult check(@RequestParam String qq, @RequestParam String token) {
        if (!token.equals(this.token)) {
            return new ExamResult(0, 0, 0, 0, "token is invalid", false);
        }
        long qqNumber = Long.parseLong(qq);
        Optional<UserTraffic> userTrafficOptional = userTrafficService.findLastByInfoContainsEntryOf(qqNumber, "action", "submitExam");
        if (userTrafficOptional.isEmpty()) {
            return new ExamResult(qqNumber, 0, 0, 0, "not examined", false);
        } else {
            UserTraffic userTraffic = userTrafficOptional.get();
            try {
                ExamResult examResult = objectMapper.readValue(userTraffic.getAttributesMap().get("examResult"), ExamResult.class);
                examResult.setMessage(null);
                if (examResult.isPassed() && Boolean.parseBoolean(settingService.get("checking.enableAutoCreateUser")) && !userService.existsByQQNumber(qqNumber)) {
                    User autoUser = new User(qq, qqNumber, null);
                    userService.save(autoUser);
                }
                return examResult;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
*/
}
