package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.partition.GetPartitionsAction;
import indi.etern.checkIn.action.setting.GetDrawingSetting;
import indi.etern.checkIn.action.setting.GetFacadeSetting;
import indi.etern.checkIn.action.setting.GetGradingSetting;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.exam.ExamGenerator;
import indi.etern.checkIn.service.exam.ExamResult;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Exam {
    private final PartitionService partitionService;
    private final QuestionService multiPartitionableQuestionService;
    private final ObjectMapper objectMapper;
    private final ActionExecutor actionExecutor;
    private final ExamGenerator examGenerator;
    
    public Exam(PartitionService partitionService, QuestionService multiPartitionableQuestionService, ObjectMapper objectMapper, /*ExamCheckService checkService, */ActionExecutor actionExecutor, ExamGenerator examGenerator) {
        this.partitionService = partitionService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.objectMapper = objectMapper;
        this.actionExecutor = actionExecutor;
        this.examGenerator = examGenerator;
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/api/generate")
    public ExamData generateExam(@RequestParam String qq, @RequestParam List<String> partitionIds, HttpServletRequest request) throws Exception {
        return examGenerator.generateExam(Long.parseLong(qq),partitionService.findAllByIds(partitionIds.stream().map(Integer::parseInt).toList()));
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/api/reload")
    public ExamData loadLastExam(@RequestParam String qq, HttpServletRequest request) throws JsonProcessingException, BadRequestException {
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/api/submit")
    public ExamResult submit(@RequestBody Map<String, Object> questionMap, HttpServletRequest request) throws BadRequestException, JsonProcessingException {
        return new ExamResult();
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET, path = "/api/examData")
    public Map<String, Object> getFacadeData() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> facadeSettingMap = actionExecutor.executeByTypeClass(GetFacadeSetting.class).orElseThrow();
        Map<String, Object> drawingSettingMap = actionExecutor.executeByTypeClass(GetDrawingSetting.class).orElseThrow();
        Map<String, Object> gradingSettingMap = actionExecutor.executeByTypeClass(GetGradingSetting.class).orElseThrow();
        
        Map<String, Object> partitions = actionExecutor.executeByTypeClass(GetPartitionsAction.class).orElseThrow();
        ArrayList<LinkedHashMap<String,Object>> partitionsList = (ArrayList<LinkedHashMap<String, Object>>) partitions.get("partitions");
        Map<String, String> partitionsNameMap = new HashMap<>();
        partitionsList.forEach((partition) -> {
            partitionsNameMap.put(partition.get("id").toString(), (String) partition.get("name"));
        });
        
        result.put("facadeData",facadeSettingMap);
        result.put("drawingData",drawingSettingMap);
        result.put("gradingData",gradingSettingMap);
        result.put("partitions",partitionsNameMap);
        return result;
    }
}