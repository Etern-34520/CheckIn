package indi.etern.checkIn.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import indi.etern.checkIn.service.exam.ExamCheckService;
import indi.etern.checkIn.service.exam.ExamResult;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ExamData {
    private final UserTrafficService userTrafficService;
    private final PartitionService partitionService;
    private final MultiPartitionableQuestionService multiPartitionableQuestionService;
    private final ObjectMapper objectMapper;
    private final ExamCheckService checkService;
    
    public ExamData(UserTrafficService userTrafficService, PartitionService partitionService, MultiPartitionableQuestionService multiPartitionableQuestionService, ObjectMapper objectMapper, ExamCheckService checkService) {
        this.userTrafficService = userTrafficService;
        this.partitionService = partitionService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.objectMapper = objectMapper;
        this.checkService = checkService;
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/data/reload/")
    public String generateExam(@RequestParam String qq, @RequestParam List<Integer> partitionIds, HttpServletRequest request) throws Exception {
        final long qqNumber = Long.parseLong(qq);
        int examCount = userTrafficService.count(qqNumber);
        final long seed = Long.parseLong(qq + "00" + examCount);
        final Random random = new Random(seed);
        List<MultiPartitionableQuestion> multiPartitionableQuestions = partitionService.generateExam(partitionIds, random);
        Collections.shuffle(multiPartitionableQuestions, random);
        List<String> examQuestionIds = new ArrayList<>();
        for (MultiPartitionableQuestion multiPartitionableQuestion : multiPartitionableQuestions) {
            examQuestionIds.add(multiPartitionableQuestion.getMd5());
        }
        request.setAttribute("action", "generateExam");
        request.setAttribute("randomSeed", String.valueOf(seed));
        request.setAttribute("examQuestionIds", objectMapper.writeValueAsString(examQuestionIds));
        userTrafficService.log(qqNumber, request);
        try {
            return objectMapper.writeValueAsString(multiPartitionableQuestionService.findAllById(examQuestionIds));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/data/")
    public String loadLastExam(@RequestParam String qq, HttpServletRequest request) throws JsonProcessingException, BadRequestException {
        final long qqNumber = Long.parseLong(qq);
        final Optional<UserTraffic> lastTraffic = userTrafficService.findLastByInfoContainsEntryOf(qqNumber, "action", "generateExam");
        if (lastTraffic.isEmpty())
            throw new BadRequestException("No exam has generated.");
        else {
            String finished = lastTraffic.get().getAttributesMap().get("finished");
            if (finished!=null && finished.equals("true"))
                throw new BadRequestException("No exam active.");
        }
        //noinspection unchecked
        List<String> questionIds = objectMapper.readValue(lastTraffic.get().getAttributesMap().get("examQuestionIds"), List.class);
        request.setAttribute("action", "reloadExam");
        List<MultiPartitionableQuestion> questions = multiPartitionableQuestionService.findAllById(questionIds);
        userTrafficService.log(qqNumber, request);
        return objectMapper.writeValueAsString(questions);
    }
    
    @RequestMapping(method = RequestMethod.POST,path = "/exam/submit/")
    @ResponseBody
    public ExamResult submit(@RequestBody Map<String, Object> questionMap, HttpServletRequest request) throws BadRequestException, JsonProcessingException {
        final long qq = Long.parseLong((String) questionMap.get("qq"));
        //noinspection unchecked
        ExamResult result = checkService.check(qq,(Map<String, Object>) questionMap.get("data"));
        request.setAttribute("qq", String.valueOf(qq));
        request.setAttribute("action", "submitExam");
        request.setAttribute("score", String.valueOf(result.getScore()));
        request.setAttribute("examResult",objectMapper.writeValueAsString(result));
        request.setAttribute("examQuestionIds",objectMapper.writeValueAsString(result.getQuestionIds()));
        request.setAttribute("passed",String.valueOf(result.isPassed()));
        final Optional<UserTraffic> lastTraffic = userTrafficService.findLastByInfoContainsEntryOf(qq, "action", "generateExam");
        if (lastTraffic.isEmpty())
            throw new BadRequestException("No exam generated.");
        else {
            UserTraffic userTraffic = lastTraffic.get();
            userTraffic.getAttributesMap().put("finished", "true");
            userTrafficService.save(userTraffic);
        }
        userTrafficService.log(qq, request);
        return result;
    }
}