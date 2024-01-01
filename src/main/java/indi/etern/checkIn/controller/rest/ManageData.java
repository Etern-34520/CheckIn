package indi.etern.checkIn.controller.rest;

import com.google.gson.Gson;
import indi.etern.checkIn.auth.AuthException;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.DateTrafficService;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.servlet.http.HttpServletRequest;
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
    
    @PostMapping(path = "/updateQuestion/")
    @ResponseBody
    public synchronized String updateQuestion(HttpServletRequest httpServletRequest) {
        try {
            final String md5 = httpServletRequest.getParameter("md5");
            final MultiPartitionableQuestion oldQuestion = multiPartitionableQuestionService.getByMD5(md5);
            final String authorQQString = httpServletRequest.getParameter("author");
            User author;
            if (authorQQString == null) {
                if (oldQuestion == null) {
                    author = (User) httpServletRequest.getAttribute("currentUser");
                } else {
                    author = oldQuestion.getAuthor();
                }
            } else {
                author = userService.findByQQNumber(Long.parseLong(authorQQString)).orElseThrow();
            }
            final boolean enabled = Boolean.parseBoolean(httpServletRequest.getParameter("enabled"));
            checkPermission(oldQuestion, (User) httpServletRequest.getAttribute("currentUser"), author, enabled);
            
            final MultiPartitionableQuestion multiPartitionableQuestion = MultiPartitionableQuestionService.buildQuestionFromRequest(httpServletRequest, md5, author);
            multiPartitionableQuestionService.update(multiPartitionableQuestion);
            
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("type", "updateQuestion");
            dataMap.put("question", multiPartitionableQuestion.toJsonData());
            webSocketService.sendMessageToAll(gson.toJson(dataMap));
//            multiPartitionableQuestionService.save(multiPartitionableQuestion);
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (!(e instanceof AuthException))
                e.printStackTrace();
            return "error:" + e.getMessage();
        }
        return "success";
    }
    
    private static void checkPermission(MultiPartitionableQuestion oldQuestion,User currectUser, User author, boolean enabled) {
        if (oldQuestion != null && !oldQuestion.getAuthor().equals(currectUser) &&
                !JwtTokenProvider.currentUserHasPermission("edit others question")) {
            throw new AuthException("permission denied: edit others question");
        }
        if ( ( ( oldQuestion != null && !oldQuestion.getAuthor().equals(author) ) || ( oldQuestion == null && !currectUser.equals(author) ))
                && !JwtTokenProvider.currentUserHasPermission("change author")) {
            throw new AuthException("permission denied: change author");
        }
        if (((oldQuestion != null && enabled != oldQuestion.isEnabled())
                ||(oldQuestion == null && enabled))
                && !JwtTokenProvider.currentUserHasPermission("enable and disable question")) {
            throw new AuthException("permission denied: enable and disable question");
        }
    }
    
}
