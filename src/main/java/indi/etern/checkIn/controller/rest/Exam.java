package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.partition.GetPartitions;
import indi.etern.checkIn.action.setting.GetDrawingSetting;
import indi.etern.checkIn.action.setting.GetFacadeSetting;
import indi.etern.checkIn.action.setting.GetGradingSetting;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.*;

@RestController
public class Exam {
    private final PartitionService partitionService;
    private final QuestionService multiPartitionableQuestionService;
    private final ObjectMapper objectMapper;
    private final ActionExecutor actionExecutor;
    private final Random random = new SecureRandom();
    
    public Exam(PartitionService partitionService, QuestionService multiPartitionableQuestionService, ObjectMapper objectMapper, /*ExamCheckService checkService, */ActionExecutor actionExecutor) {
        this.partitionService = partitionService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.objectMapper = objectMapper;
        this.actionExecutor = actionExecutor;
    }
    
/*
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/api/generate")
    public String generateExam(@RequestParam String qq, @RequestParam List<Integer> partitionIds, HttpServletRequest request) throws Exception {
    }
*/

/*
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/api/reload")
    public String loadLastExam(@RequestParam String qq, HttpServletRequest request) throws JsonProcessingException, BadRequestException {
    }
*/

/*
    @RequestMapping(method = RequestMethod.POST, path = "/api/submit")
    @ResponseBody
    public ExamResult submit(@RequestBody Map<String, Object> questionMap, HttpServletRequest request) throws BadRequestException, JsonProcessingException {
    }
*/
    
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET, path = "/api/examData")
    public Map<String, Object> getFacadeData() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> facadeSettingMap = ((Optional<Map<String, Object>>) actionExecutor.executeByTypeClass(GetFacadeSetting.class)).orElseThrow();
        Map<String, Object> drawingSettingMap = ((Optional<Map<String, Object>>) actionExecutor.executeByTypeClass(GetDrawingSetting.class)).orElseThrow();
        Map<String, Object> gradingSettingMap = ((Optional<Map<String, Object>>) actionExecutor.executeByTypeClass(GetGradingSetting.class)).orElseThrow();
        
        Map<String, Object> partitions = ((Optional<Map<String, Object>>) actionExecutor.executeByTypeClass(GetPartitions.class)).orElseThrow();
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