package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import indi.etern.checkIn.service.exam.ExamResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class CheckApi {
    private final UserTrafficService userTrafficService;
    private final ObjectMapper objectMapper;
    private final SettingService settingService;
    private final UserService userService;

    @Value("${checkIn.robotToken}")
    private String token;

    public CheckApi(UserTrafficService userTrafficService, ObjectMapper objectMapper, SettingService settingService, UserService userService) {
        this.userTrafficService = userTrafficService;
        this.objectMapper = objectMapper;
        this.settingService = settingService;
        this.userService = userService;
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
}
