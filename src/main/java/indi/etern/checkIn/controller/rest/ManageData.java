package indi.etern.checkIn.controller.rest;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.DateTrafficService;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "manage/data")
public class ManageData {
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    DateTrafficService dateTrafficService;
    @Autowired
    UserTrafficService userTrafficService;
    @Autowired
    MultiPartitionableQuestionService multiPartitionableQuestionService;
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    UserService userService;
    @Autowired
    Gson gson;
    
    @GetMapping(params = "type=traffic")
    public String getTrafficJsonData() {
        final LocalDate now = LocalDate.now();
        List<DateTraffic> dateTraffics = dateTrafficService.getAllFromTo(now.minusDays(6), now);
        List<Map<String, String>> jsonData = new ArrayList<>();
        for (DateTraffic dateTraffic : dateTraffics) {
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("count", String.valueOf(dateTraffic.getCount()));
            hashMap.put("date", dateTraffic.getLocalDate().getMonthValue() + "-" + dateTraffic.getLocalDate().getDayOfMonth());
            jsonData.add(hashMap);
        }
        return gson.toJson(jsonData);
    }
    
    @GetMapping(params = {"type=trafficDetail", "pageIndex"})
    public String getTrafficDetailData(int pageIndex) {
        Sort sort = Sort.by(Sort.Direction.DESC, "localDate");
        Pageable pageable = PageRequest.of(pageIndex, 20, sort);
        DateTraffic dateTraffic = dateTrafficService.getByLocalDate(LocalDate.now().minusDays(pageIndex));
        List<Map<String, String>> jsonData = new ArrayList<>();
        for (UserTraffic userTraffic : dateTraffic.getUserTraffics()) {
            Map<String, String> userTrafficDataMap = userTraffic.getHeaderInfo();
            userTrafficDataMap.put("ip", userTraffic.getIP());
            userTrafficDataMap.put("qq", String.valueOf(userTraffic.getQQNumber()));
            userTrafficDataMap.put("date", userTraffic.getLocalDate().toString());
            userTrafficDataMap.put("time", userTraffic.getLocalTime().toString());
            userTrafficDataMap.put("dateTime", userTraffic.getLocalDateTime().toString());
            jsonData.add(userTrafficDataMap);
        }
        return gson.toJson(jsonData);
    }
    
    @PostMapping(path = "/updateQuestion/{questionMD5}")
    @ResponseBody
    public synchronized String updateQuestion(HttpServletRequest httpServletRequest, @PathVariable String questionMD5) {
        try {
            MultipleQuestionBuilder multipleQuestionBuilder = new MultipleQuestionBuilder();
            final String md5 = httpServletRequest.getParameter("md5");
            if (md5 != null) {
                multipleQuestionBuilder.setMD5(md5);
            }
            Map<String, String[]> map = httpServletRequest.getParameterMap();
            
            //content
            multipleQuestionBuilder.setQuestionContent(httpServletRequest.getParameter("questionContent"));
            
            //image
            int imageIndex = 0;
            while (true) {
                final Part part = httpServletRequest.getPart("image_" + imageIndex);
                if (part == null) {
                    break;
                }
                multipleQuestionBuilder.addImage(part);
                imageIndex++;
            }
            
            //choices&partition
            Map<Integer, Object[]> choiceParamMap = new HashMap<>();
            for (String key : map.keySet()) {
                if (key.matches("-?\\d+(\\.\\d+)?")) {//为数字
                    String choiceContent = map.get(key)[0];
                    choiceParamMap.put(Integer.parseInt(key), new Object[]{choiceContent, false});
                } else if (key.startsWith("question_partition_") && map.get(key)[0].equals("true")){
                    int partitionId = Integer.parseInt(key.substring(19));
//                    multipleQuestionBuilder.addPartition(partitionName);
                    multipleQuestionBuilder.addPartition(partitionId);
                }
            }
            for (String key : map.keySet()) {
                if (key.startsWith("correct")) {
                    final String indexString = key.substring(7);
                    if (indexString.matches("-?\\d+(\\.\\d+)?")) {
                        int index = Integer.parseInt(indexString);
                        choiceParamMap.get(index)[1] = map.get(key)[0].equals("true");
                    }
                }
            }
            List<Choice> choices = new ArrayList<>();
            for (Object[] choiceParam : choiceParamMap.values()) {
                Choice choice = new Choice((String) choiceParam[0], (Boolean) choiceParam[1]);
                choices.add(choice);
            }
            multipleQuestionBuilder.addChoices(choices);
            
            User author = userService.findByQQNumber(Long.parseLong(httpServletRequest.getParameter("author"))).orElseThrow();
            multipleQuestionBuilder.setAuthor(author);
            
            multipleQuestionBuilder.setEnable(Boolean.parseBoolean(httpServletRequest.getParameter("enabled")));
            
            MultiPartitionableQuestion multiPartitionableQuestion = multipleQuestionBuilder.build();
            multiPartitionableQuestionService.update(multiPartitionableQuestion);
            
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("type", "updateQuestion");
            dataMap.put("question", multiPartitionableQuestion.toJsonData());
            webSocketService.sendMessageToAll(gson.toJson(dataMap));
//            multiPartitionableQuestionService.save(multiPartitionableQuestion);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return "error";
        }
        return "success";
    }
    
}
