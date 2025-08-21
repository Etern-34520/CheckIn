package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.interfaces.ResultContext;
import indi.etern.checkIn.action.partition.GetPartitionsAction;
import indi.etern.checkIn.action.setting.get.GetFacadeSetting;
import indi.etern.checkIn.action.setting.get.GetGradingSetting;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.service.dao.*;
import indi.etern.checkIn.service.exam.ExamGenerator;
import indi.etern.checkIn.service.exam.ExamResult;
import indi.etern.checkIn.service.web.TurnstileService;
import indi.etern.checkIn.throwable.entity.UserExistsException;
import indi.etern.checkIn.throwable.exam.ExamException;
import indi.etern.checkIn.throwable.exam.ExamIllegalStateException;
import indi.etern.checkIn.throwable.exam.ExamSubmittedException;
import indi.etern.checkIn.throwable.exam.generate.ExamGenerateFailedException;
import indi.etern.checkIn.throwable.exam.generate.PartitionsOutOfRangeException;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ExamController {
    private final PartitionService partitionService;
    private final ActionExecutor actionExecutor;
    private final ExamGenerator examGenerator;
    private final ExamDataService examDataService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(ExamController.class);
    private final String EXAM_IS_NOT_EXIST_JSON = "{\"type\":\"error\",\"message\":\"Exam is not exist\"}";
    private final String EXAM_INVALIDED_JSON = "{\"type\":\"error\",\"message\":\"Exam invalided\"}";
    private final String EXAM_SUBMITTED_JSON = "{\"type\":\"error\",\"message\":\"Exam has already submitted\"}";
    private final String EXAM_NOT_SUBMITTED_JSON = "{\"type\":\"error\",\"message\":\"Exam has not been submitted\"}";
    private final String USER_EXISTS_JSON = "{\"type\":\"error\",\"message\":\"User already exists\"}";
    private final String NOT_SUPPORTED_LEVEL_FOR_SIGN_UP_JSON = "{\"type\":\"error\",\"message\":\"Not a supported level for sign up\"}";
    private final QuestionStatisticService questionStatisticService;
    private final SettingService settingService;
    private final UserService userService;
    private final GradingLevelService gradingLevelService;
    private final TurnstileService turnstileService;

    public ExamController(PartitionService partitionService, ActionExecutor actionExecutor, ExamGenerator examGenerator,
                          ExamDataService examDataService, ObjectMapper objectMapper, QuestionStatisticService questionStatisticService,
                          SettingService settingService, UserService userService, GradingLevelService gradingLevelService,
                          TurnstileService turnstileService) {
        this.partitionService = partitionService;
        this.actionExecutor = actionExecutor;
        this.examGenerator = examGenerator;
        this.examDataService = examDataService;
        this.objectMapper = objectMapper;
        this.questionStatisticService = questionStatisticService;
        this.settingService = settingService;
        this.userService = userService;
        this.gradingLevelService = gradingLevelService;
        this.turnstileService = turnstileService;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenerateRequest(long qq, List<String> partitionIds, String turnstileToken) {
    }
    
    @PostMapping(path = "/api/generate")
    @Transactional(noRollbackFor = Throwable.class)
    public String generateExam(@RequestBody GenerateRequest generateRequest, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        if (turnstileService.isTurnstileEnabledOnExam() && turnstileService.isServiceEnable()) {
            try {
                turnstileService.check(generateRequest.turnstileToken, httpServletRequest);
            } catch (Exception e) {
                Map<String, String> errorDataMap = new HashMap<>();
                errorDataMap.put("type", "error");
                errorDataMap.put("description", e.getMessage());
                errorDataMap.put("exceptionType", e.getClass().getSimpleName());
                return objectMapper.writeValueAsString(errorDataMap);
            }
        }
        try {
            List<Integer> range = null;
            try {
                SettingItem item = settingService.getItem("generating","partitionRange");
                final ArrayList<?> value = item.getValue(ArrayList.class);
                if (value.size() == 2) {
                    //noinspection unchecked
                    range = (List<Integer>) value;
                } else {
                    logger.warn("Setting \"generating.partitionRange\" not match to 2 elements");
                }
            } catch (Exception ignored) {
                logger.warn("Setting \"generating.partitionRange\" missing");
            }
            final int size = generateRequest.partitionIds.size();
            if (range == null || (size >= range.getFirst() && size <= range.getLast())) {
                final ExamData examData = examGenerator.generateExam(generateRequest.qq, partitionService.findAllByIds(generateRequest.partitionIds));
                examDataService.invalidAllByQQ(generateRequest.qq);
                examDataService.save(examData);
                questionStatisticService.appendStatistic(examData);
                Map<String, Object> result = new HashMap<>();
                result.put("examId", examData.getId());
                result.put("questionItemCount", examData.getQuestionIds().size());
                examData.sendUpdateExamRecord();
                return objectMapper.writeValueAsString(result);
            } else {
                throw new PartitionsOutOfRangeException();
            }
        } catch (ExamGenerateFailedException e) {
            Map<String, String> errorDataMap = new HashMap<>();
            errorDataMap.put("type", "error");
            errorDataMap.put("description", e.getDescription());
            errorDataMap.put("exceptionType", e.getClass().getSimpleName());
            return objectMapper.writeValueAsString(errorDataMap);
        }
    }
    
    public record GetQuestionsByIndexRequest(String examId, int[] indexes) {
    }
    
    @RequestMapping(method = RequestMethod.POST, path = "/api/exam-questions", produces = "application/json;charset=UTF-8")
    @Transactional(propagation = Propagation.NESTED)
    public String getQuestionsByExamIdAndIndexes(@RequestBody GetQuestionsByIndexRequest request) throws JsonProcessingException, ExamException {
        Optional<ExamData> optionalExamData = examDataService.findById(request.examId);
        if (optionalExamData.isPresent()) {
            try {
                final Map<String, Object> result = examDataService.getExamDataQuestions(request.indexes, optionalExamData.get());
                return objectMapper.writeValueAsString(result);
            } catch (ExamInvalidException e) {
                return EXAM_INVALIDED_JSON;
            } catch (ExamSubmittedException e) {
                return EXAM_SUBMITTED_JSON;
            }
        } else {
            return EXAM_IS_NOT_EXIST_JSON;
        }
    }
    
    public record GetResultRequest(String examId) {}
    
    @SneakyThrows
    @Transactional
    @RequestMapping(method = RequestMethod.POST, path = "/api/get-result")
    public String getResult(@RequestBody GetResultRequest getResultRequest) {
        Optional<ExamData> optionalExamData = examDataService.findById(getResultRequest.examId);
        if (optionalExamData.isPresent()) {
            final ExamData examData = optionalExamData.get();
            final ExamResult examResult = examData.getExamResult();
            if (examResult != null) {
                return objectMapper.writeValueAsString(examResult);
            } else {
                return EXAM_NOT_SUBMITTED_JSON;
            }
        } else {
            return EXAM_IS_NOT_EXIST_JSON;
        }
    }
    
    public record SubmitRequest(String examId, Map<String, Object> answer) {
    }
        
    @SneakyThrows
    @Transactional
    @RequestMapping(method = RequestMethod.POST, path = "/api/submit")
    public String submit(@RequestBody SubmitRequest submitRequest) {
        Optional<ExamData> optionalExamData = examDataService.findById(submitRequest.examId);
        if (optionalExamData.isPresent()) {
            final ExamData examData = optionalExamData.get();
            final ExamResult examResult1 = examData.getExamResult();
            if (examResult1 != null) {
                return EXAM_SUBMITTED_JSON;
            } else {
                try {
                    final ExamResult examResult = examDataService.handleSubmit(examData, submitRequest.answer);
                    if (userService.existsByQQNumber(examResult.getQq())) {
                        examResult.setSignUpCompletingType("USER_EXISTS");
                    }
                    examData.sendUpdateExamRecord();
                    questionStatisticService.appendStatistic(examData);
                    return objectMapper.writeValueAsString(examResult);
                } catch (ExamInvalidException e) {
                    logger.error("ExamController[{}] invalided", examData.getId());
                    return EXAM_INVALIDED_JSON;
//                throw new BadRequestException();
                }
            }
        } else {
            logger.error("Could not found examData({})", submitRequest.examId);
            return EXAM_IS_NOT_EXIST_JSON;
//            throw new BadRequestException();
        }
    }
    
    @Transactional
    @RequestMapping(method = RequestMethod.GET, path = "/api/exam-data")
    public Map<String, Object> getData() {
        Map<String, Object> result = new HashMap<>();
        var facadeSettingContext = actionExecutor.execute(GetFacadeSetting.class);
        var gradingSettingContext = actionExecutor.execute(GetGradingSetting.class);
        
        final Boolean showRequiredPartitions = settingService.getItem("generating", "showRequiredPartitions").getValue(Boolean.class);
        //noinspection unchecked
        final List<String> requiredPartitionIds = settingService.getItem("generating", "requiredPartitions").getValue(ArrayList.class);
        Set<String> requiredPartitionIdSet = new HashSet<>(requiredPartitionIds);
        List<String> selectablePartitionIds = new ArrayList<>();
        
        ResultContext<GetPartitionsAction.Output> context = actionExecutor.execute(GetPartitionsAction.class);
        final Map<String, String> usedPartitionsNameMap = getPartitionsNameMap(context, requiredPartitionIdSet, selectablePartitionIds);
        
        final GetFacadeSetting.SuccessOutput output = facadeSettingContext.getOutput();
        result.put("facadeData", output.data());
//        result.put("generatingData", generatingSettingMap);
        result.put("gradingData", gradingSettingContext.getOutput().data());
        
        Map<String, Object> extraData = output.extraData();
        extraData.put("partitions", usedPartitionsNameMap);
        extraData.put("selectablePartitionIds", selectablePartitionIds);
        if (showRequiredPartitions)
            extraData.put("requiredPartitionIds", requiredPartitionIds);
        result.put("extraData", extraData);
        return result;
    }
    
    private Map<String, String> getPartitionsNameMap(ResultContext<GetPartitionsAction.Output> context, Set<String> requiredPartitionIdSet, List<String> selectablePartitionIds) {
        List<Partition> partitions = context.getOutput().partitions();
        Map<String, String> usedPartitionsNameMap = new HashMap<>();
        partitions.forEach((partition) -> {
            if (!requiredPartitionIdSet.contains(partition.getId())) {
                if (partition.getEnabledQuestionCount() != 0) {
                    selectablePartitionIds.add(partition.getId());
                    usedPartitionsNameMap.put(partition.getId(), partition.getName());
                }
            } else {
                usedPartitionsNameMap.put(partition.getId(), partition.getName());
            }
        });
        return usedPartitionsNameMap;
    }
    
    public record SignUpRequest(String examId, String name, String password) {}
    
    @Transactional()
    @RequestMapping(method = RequestMethod.POST, path = "/api/sign-up")
    public String signUpWith(@RequestBody SignUpRequest signUpRequest) throws JsonProcessingException, ExamException {
        final Optional<ExamData> optionalExamData = examDataService.findById(signUpRequest.examId);
        if (optionalExamData.isPresent()) {
            ExamData examData = optionalExamData.get();
            try {
                final String levelId = examData.getExamResult().getLevelId();
                GradingLevel level = gradingLevelService.findById(levelId);
                final GradingLevel.CreatingUserStrategy creatingUserStrategy = level.getCreatingUserStrategy();
                if (creatingUserStrategy == GradingLevel.CreatingUserStrategy.NOT_CREATE) {
                    return NOT_SUPPORTED_LEVEL_FOR_SIGN_UP_JSON;
                } else {
                    final boolean enabled = creatingUserStrategy == GradingLevel.CreatingUserStrategy.CREATE_AND_ENABLED;
                    userService.handleSignUp(examData, signUpRequest.name, signUpRequest.password, level.getCreatingUserRole(), enabled);
                    examData.setStatus(ExamData.Status.SIGN_UP_COMPLETED);
                    examData.sendUpdateExamRecord();
                    examDataService.save(examData);
                    Map<String,String> message = new HashMap<>();
                    message.put("type", "success");
                    message.put("message", "Signed up successfully");
                    message.put("completingType", creatingUserStrategy.name());
                    return objectMapper.writeValueAsString(message);
                }
            } catch (ExamIllegalStateException e) {
                return EXAM_NOT_SUBMITTED_JSON;
            } catch (ExamInvalidException e) {
                return EXAM_INVALIDED_JSON;
            } catch (ExamException e) {
                throw e;
            } catch (UserExistsException e) {
                return USER_EXISTS_JSON;
            } catch (Exception e) {
                Map<String,String> message = new HashMap<>();
                message.put("type", "error");
                message.put("message", e.getMessage());
                return objectMapper.writeValueAsString(message);
            }
        } else {
            return EXAM_IS_NOT_EXIST_JSON;
        }
    }
}