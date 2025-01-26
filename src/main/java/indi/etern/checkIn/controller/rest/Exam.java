package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.partition.GetPartitionsAction;
import indi.etern.checkIn.action.setting.GetDrawingSetting;
import indi.etern.checkIn.action.setting.GetFacadeSetting;
import indi.etern.checkIn.action.setting.GetGradingSetting;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.service.dao.ExamDataService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.exam.ExamGenerator;
import indi.etern.checkIn.throwable.exam.generate.ExamGenerateFailedException;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Exam {
    private final PartitionService partitionService;
    private final ActionExecutor actionExecutor;
    private final ExamGenerator examGenerator;
    private final ExamDataService examDataService;
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(Exam.class);
    private final String EXAM_IS_NOT_EXIST_JSON = "{\"type\":\"error\",\"message\":\"Exam is not exist\"}";
    private final String EXAM_INVALIDED_JSON = "{\"type\":\"error\",\"message\":\"Exam invalided\"}";
    
    public Exam(PartitionService partitionService, ActionExecutor actionExecutor, ExamGenerator examGenerator, ExamDataService examDataService, QuestionService questionService, ObjectMapper objectMapper) {
        this.partitionService = partitionService;
        this.actionExecutor = actionExecutor;
        this.examGenerator = examGenerator;
        this.examDataService = examDataService;
        this.questionService = questionService;
        this.objectMapper = objectMapper;
    }
    
    public record GenerateRequest(long qq, List<String> partitionIds) {
    }
    
    @PostMapping(path = "/api/generate")
    @Transactional
    public Map<String, ?> generateExam(@RequestBody GenerateRequest generateRequest) {
        try {
            final ExamData examData = examGenerator.generateExam(generateRequest.qq, partitionService.findAllByIds(generateRequest.partitionIds.stream().map(Integer::parseInt).toList()));
            Map<String, Object> result = new HashMap<>();
            result.put("examId", examData.getId());
            result.put("questionItemCount", examData.getQuestionIds().size());
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
    
    public record GetQuestionsByIndexRequest(String examId, int[] indexes) {
    }
    
    @RequestMapping(method = RequestMethod.POST, path = "/api/examQuestions", produces = "application/json;charset=UTF-8")
    @Transactional(propagation = Propagation.NESTED)
    public String getQuestionsByExamIdAndIndexes(@RequestBody GetQuestionsByIndexRequest request) throws JsonProcessingException {
        Optional<ExamData> optionalExamData = examDataService.findById(request.examId);
        if (optionalExamData.isPresent()) {
            try {
                final Map<String, Object> result = getExamDataQuestions(request, optionalExamData.get());
                return objectMapper.writeValueAsString(result);
            } catch (ExamInvalidException e) {
                return EXAM_INVALIDED_JSON;
            }
        } else {
            return EXAM_IS_NOT_EXIST_JSON;
        }
    }
    
    private Map<String, Object> getExamDataQuestions(GetQuestionsByIndexRequest request, ExamData examData) throws ExamInvalidException {
        if (examData.getStatus() == ExamData.Status.ONGOING) {
            Map<String, Object> result = new HashMap<>();
            List<String> allQuestionIds = examData.getQuestionIds();
            List<String> questionIds = new ArrayList<>();
            for (int index : request.indexes) {
                questionIds.add(allQuestionIds.get(index));
            }
            final List<Question> questions = new ArrayList<>(questionService.findAllById(questionIds));
            if (questions.size() != request.indexes.length) {
                logger.warn("some questions of the exam is missing, remediate exam");
                examGenerator.remediateExam(examData);
                return getExamDataQuestions(request, examData);
//            throw new RuntimeException("Questions not found");
            }
            Map<Integer, Question> indexQuestionMap = new HashMap<>();
            questions.forEach(question -> indexQuestionMap.put(allQuestionIds.indexOf(question.getId()), question));
            result.put("questions", indexQuestionMap);
            return result;
        } else {
            logger.error("Exam[{}] invalided", examData.getId());
            throw new ExamInvalidException();
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
    @RequestMapping(method = RequestMethod.POST, path = "/api/submit")
    public String submit(@RequestBody SubmitRequest submitRequest) {
        Optional<ExamData> optionalExamData = examDataService.findById(submitRequest.examId);
        if (optionalExamData.isPresent()) {
            final ExamData examData = optionalExamData.get();
            try {
                return objectMapper.writeValueAsString(examDataService.handleSubmit(examData, submitRequest.answer));
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
        Map<String, Object> facadeSettingMap = actionExecutor.executeByTypeClass(GetFacadeSetting.class).orElseThrow();
        Map<String, Object> drawingSettingMap = actionExecutor.executeByTypeClass(GetDrawingSetting.class).orElseThrow();
        Map<String, Object> gradingSettingMap = actionExecutor.executeByTypeClass(GetGradingSetting.class).orElseThrow();
        
        Map<String, Object> partitions = actionExecutor.executeByTypeClass(GetPartitionsAction.class).orElseThrow();
        ArrayList<LinkedHashMap<String, Object>> partitionsList = (ArrayList<LinkedHashMap<String, Object>>) partitions.get("partitions");
        Map<String, String> partitionsNameMap = new HashMap<>();
        partitionsList.forEach((partition) -> {
            partitionsNameMap.put(partition.get("id").toString(), (String) partition.get("name"));
        });
        
        result.put("facadeData", facadeSettingMap);
        result.put("drawingData", drawingSettingMap);
        result.put("gradingData", gradingSettingMap);
        result.put("partitions", partitionsNameMap);
        return result;
    }
}