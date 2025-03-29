package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.interfaces.ResultContext;
import indi.etern.checkIn.action.partition.GetPartitionsAction;
import indi.etern.checkIn.action.setting.get.GetFacadeSetting;
import indi.etern.checkIn.action.setting.get.GetGradingSetting;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionStatisticService;
import indi.etern.checkIn.service.exam.ExamGenerator;
import indi.etern.checkIn.service.exam.ExamResult;
import indi.etern.checkIn.throwable.exam.generate.ExamGenerateFailedException;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class Exam {
    private final PartitionService partitionService;
    private final ActionExecutor actionExecutor;
    private final ExamGenerator examGenerator;
    private final ExamDataService examDataService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(Exam.class);
    private final String EXAM_IS_NOT_EXIST_JSON = "{\"type\":\"error\",\"message\":\"Exam is not exist\"}";
    private final String EXAM_INVALIDED_JSON = "{\"type\":\"error\",\"message\":\"Exam invalided\"}";
    private final QuestionStatisticService questionStatisticService;
    
    public Exam(PartitionService partitionService, ActionExecutor actionExecutor, ExamGenerator examGenerator, ExamDataService examDataService, ObjectMapper objectMapper, QuestionStatisticService questionStatisticService) {
        this.partitionService = partitionService;
        this.actionExecutor = actionExecutor;
        this.examGenerator = examGenerator;
        this.examDataService = examDataService;
        this.objectMapper = objectMapper;
        this.questionStatisticService = questionStatisticService;
    }
    
    public record GenerateRequest(long qq, List<String> partitionIds) { }
    
    @PostMapping(path = "/api/generate")
    @Transactional(noRollbackFor = Throwable.class)
    public Map<String, ?> generateExam(@RequestBody GenerateRequest generateRequest) {
        try {
            final ExamData examData = examGenerator.generateExam(generateRequest.qq, partitionService.findAllByIds(generateRequest.partitionIds));
            examDataService.invalidAllByQQ(generateRequest.qq);
            examDataService.save(examData);
            questionStatisticService.appendStatistic(examData);
            Map<String, Object> result = new HashMap<>();
            result.put("examId", examData.getId());
            result.put("questionItemCount", examData.getQuestionIds().size());
            examData.sendUpdateExamRecord();
            return result;
        } catch (ExamGenerateFailedException e) {
            Map<String,String> errorDataMap = new HashMap<>();
            errorDataMap.put("type","error");
            errorDataMap.put("enDescription",e.getEnDescription());
            errorDataMap.put("cnDescription",e.getCnDescription());
            errorDataMap.put("exceptionType",e.getClass().getSimpleName());
            return errorDataMap;
        }
    }
    
    public record GetQuestionsByIndexRequest(String examId, int[] indexes) {}
    
    @RequestMapping(method = RequestMethod.POST, path = "/api/examQuestions", produces = "application/json;charset=UTF-8")
    @Transactional(propagation = Propagation.NESTED)
    public String getQuestionsByExamIdAndIndexes(@RequestBody GetQuestionsByIndexRequest request) throws JsonProcessingException {
        Optional<ExamData> optionalExamData = examDataService.findById(request.examId);
        if (optionalExamData.isPresent()) {
            try {
                final Map<String, Object> result = examDataService.getExamDataQuestions(request.indexes, optionalExamData.get());
                return objectMapper.writeValueAsString(result);
            } catch (ExamInvalidException e) {
                return EXAM_INVALIDED_JSON;
            }
        } else {
            return EXAM_IS_NOT_EXIST_JSON;
        }
    }
    
    /*
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/api/reload")
    public ExamData loadLastExam(@RequestParam String qq, HttpServletRequest request) {
        return null;
    }
*/
    
    public record SubmitRequest(String examId, Map<String, Object> answer) {
    }
    
    @SneakyThrows
    @Transactional
    @RequestMapping(method = RequestMethod.POST, path = "/api/submit")
    public String submit(@RequestBody SubmitRequest submitRequest) {
        Optional<ExamData> optionalExamData = examDataService.findById(submitRequest.examId);
        if (optionalExamData.isPresent()) {
            final ExamData examData = optionalExamData.get();
            try {
                final ExamResult examResult = examDataService.handleSubmit(examData, submitRequest.answer);
                examData.sendUpdateExamRecord();
                return objectMapper.writeValueAsString(examResult);
            } catch (ExamInvalidException e) {
                logger.error("Exam[{}] invalided", examData.getId());
                return EXAM_INVALIDED_JSON;
//                throw new BadRequestException();
            }
        } else {
            logger.error("Could not found examData({})", submitRequest.examId);
            return EXAM_IS_NOT_EXIST_JSON;
//            throw new BadRequestException();
        }
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET, path = "/api/examData")
    public Map<String, Object> getFacadeData() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> facadeSettingMap = actionExecutor.executeByTypeClass(GetFacadeSetting.class,null).orElseThrow();
//        Map<String, Object> generatingSettingMap = actionExecutor.executeByTypeClass(GetGeneratingSetting.class).orElseThrow();
        Map<String, Object> gradingSettingMap = actionExecutor.executeByTypeClass(GetGradingSetting.class, null).orElseThrow();
        
        ResultContext<GetPartitionsAction.Output> context = actionExecutor.execute(GetPartitionsAction.class, null);
        List<Map<String, Object>> partitionDataList = context.getOutput().partitionDataList();
        Map<String, String> partitionsNameMap = new HashMap<>();
        partitionDataList.forEach((partition) -> {
            partitionsNameMap.put(partition.get("id").toString(), (String) partition.get("name"));
        });
        
        result.put("facadeData", facadeSettingMap);
//        result.put("generatingData", generatingSettingMap);
        result.put("gradingData", gradingSettingMap);
        result.put("partitions", partitionsNameMap);
        return result;
    }
}