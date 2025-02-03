package indi.etern.checkIn.controller.rest;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class ExposeApi {
    private final ExamDataService examDataService;
    
    public ExposeApi(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    public record QualifyRequest(long qq){}
    @PostMapping(path = "/api/qualify")
    public Map<String,Object> qualify(@RequestBody QualifyRequest qualifyRequest) {
        Map<String,Object> map = new HashMap<>();
        final Optional<ExamData> optionalExamData = examDataService.findMaxByQQ(qualifyRequest.qq);
        if (optionalExamData.isPresent()) {
            ExamData examData = optionalExamData.get();
            map.put("type", "success");
            map.put("examData", examData);
            map.put("level", examData.getExamResult().getLevel());
        } else {
            map.put("type", "error");
            map.put("result", "examData not found");
        }
        return map;
    }
}
