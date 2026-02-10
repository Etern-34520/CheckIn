package indi.etern.checkIn.controller.rest;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.interfaces.ResultContext;
import indi.etern.checkIn.action.oauth2.GetOAuth2ProvidersSimpleInfoAction;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.exam.ExamResult;
import indi.etern.checkIn.service.exam.SignUpCompletingType;
import indi.etern.checkIn.service.web.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class ExposeApi {
    private final ExamDataService examDataService;
    private final GradingLevelService levelService;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final Logger logger = LoggerFactory.getLogger(ExposeApi.class);
    private final ActionExecutor actionExecutor;

    public ExposeApi(ExamDataService examDataService, GradingLevelService levelService, UserService userService, WebSocketService webSocketService, ActionExecutor actionExecutor) {
        this.examDataService = examDataService;
        this.levelService = levelService;
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.actionExecutor = actionExecutor;
    }

    @Transactional
    @PostMapping(path = "/api/qualify", produces = "application/json")
    public Map<String, Object> qualify(@RequestBody QualifyRequest qualifyRequest) {
        try {
            Map<String, Object> map = new HashMap<>();
            final Optional<ExamData> optionalExamData = examDataService.findMaxByQQ(qualifyRequest.qq);
            if (optionalExamData.isPresent()) {
                ExamData examData = optionalExamData.get();
                map.put("type", "success");
                map.put("examData", examData);
                ExamResult examResult = examData.getExamResult();
                if (examResult != null) {
                    map.put("level", examResult.getLevel());
                    map.put("levelId", examResult.getLevelId());
                    try {
                        final GradingLevel level = levelService.findById(examResult.getLevelId());
                        if (level.getCreatingUserStrategy() == GradingLevel.CreatingUserStrategy.CREATE_AND_ENABLED_AFTER_VALIDATED) {
                            userService.findByQQNumber(examData.getQqNumber()).ifPresent((user) -> {
                                user.setEnabled(true);
                                Message<User> message = Message.of("updateUser", user);
                                webSocketService.sendMessageToAll(message);
                                userService.save(user);
                            });
                            examResult.setSignUpCompletingType(SignUpCompletingType.COMPLETED);
                        }
                    } catch (Exception e) {
                        logger.error("When load grading level:{}", e.getMessage());
                    }
                }
            } else {
                map.put("type", "error");
                map.put("result", "examData not found");
            }
            return map;
        } catch (Exception e) {
            return Map.of("type", "error", "result", e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    @GetMapping("/api/oauth2/providers")
    public GetOAuth2ProvidersSimpleInfoAction.SuccessOutput getOAuth2Providers() {
        ResultContext<GetOAuth2ProvidersSimpleInfoAction.SuccessOutput> resultContext =
                actionExecutor.execute(GetOAuth2ProvidersSimpleInfoAction.class);
        if (resultContext.getOutput() instanceof GetOAuth2ProvidersSimpleInfoAction.SuccessOutput output) {
            return output;
        } else {
            throw new IllegalStateException();
        }
    }

    public record QualifyRequest(long qq) {
    }
}
