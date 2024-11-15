package indi.etern.checkIn.controller.rest;

import indi.etern.checkIn.service.dao.DateTrafficService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "manage/data")
public class ManageData {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final DateTrafficService dateTrafficService;
    final UserTrafficService userTrafficService;
    final QuestionService multiPartitionableQuestionService;
    final WebSocketService webSocketService;
    final UserService userService;
    
    public ManageData(DateTrafficService dateTrafficService, UserTrafficService userTrafficService, QuestionService multiPartitionableQuestionService, WebSocketService webSocketService, UserService userService) {
        this.dateTrafficService = dateTrafficService;
        this.userTrafficService = userTrafficService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.webSocketService = webSocketService;
        this.userService = userService;
    }
    
/*
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
*/
    
    /*@GetMapping(params = {"type=trafficDetail", "pageIndex"})
    public String getTrafficItemDataByPage(int pageIndex) {
        Sort sort = Sort.by(Sort.Direction.DESC, "localDate").and(Sort.by(Sort.Direction.DESC, "localTime"));
        Pageable pageable = PageRequest.of(pageIndex, 20, sort);
//        DateTraffic dateTraffic = dateTrafficService.getByLocalDate(LocalDate.now().minusDays(pageIndex));
//        DateTraffic dateTraffic = dateTrafficService.findAll(pageable).getContent().getFirst();
        Page<UserTraffic> userTrafficPage = userTrafficService.findAll(pageable);
        return getJson(userTrafficPage);
    }

    @GetMapping(params = {"type=trafficDetail", "id"})
    public UserTraffic getTrafficItemData(int id) {
        return userTrafficService.findById(id).orElseThrow();
    }
    
    private String getJson(Page<UserTraffic> userTrafficPage) {
        List<Map<String, String>> jsonData = new ArrayList<>();
        for (UserTraffic userTraffic : *//*dateTraffic.getUserTraffics()*//* userTrafficPage.getContent()) {
            Map<String, String> userTrafficDataMap = userTraffic.getHeaderMap();
            userTrafficDataMap.put("ip", userTraffic.getIP());
            userTrafficDataMap.put("qq", String.valueOf(userTraffic.getQQNumber()));
            userTrafficDataMap.put("date", userTraffic.getLocalDate().toString());
            userTrafficDataMap.put("time", userTraffic.getLocalTime().toString());
            userTrafficDataMap.put("dateTime", userTraffic.getLocalDateTime().toString());
            userTrafficDataMap.put("id", String.valueOf(userTraffic.getId()));
            userTrafficDataMap.putAll(userTraffic.getAttributesMap());
            jsonData.add(userTrafficDataMap);
        }
        return gson.toJson(jsonData);
    }*/
    
/*
    @PostMapping(path = "/updateQuestion/")
    @ResponseBody
    public synchronized String updateQuestion(HttpServletRequest httpServletRequest) {
        try {
            final String id = httpServletRequest.getParameter("id");
            final Question oldQuestion = multiPartitionableQuestionService.getById(id);
            final String authorQQString = httpServletRequest.getParameter("author");
            User author;
            if (authorQQString == null) {
                if (oldQuestion == null) {
                    author = (User) httpServletRequest.getAttribute("currentUser");
                } else {
                    author = oldQuestion.getAuthor();
                }
            } else {
                author = userService.findByQQNumber(Long.parseLong(authorQQString)).orElse(null);
            }
            final boolean enabled = Boolean.parseBoolean(httpServletRequest.getParameter("enabled"));
            checkPermission(oldQuestion, (User) httpServletRequest.getAttribute("currentUser"), author, enabled);
            
            final Question multiPartitionableQuestion = QuestionService.buildQuestionFromRequest(httpServletRequest, id, author);
//            multiPartitionableQuestionService.save(multiPartitionableQuestion);
            multiPartitionableQuestionService.saveAndFlush(multiPartitionableQuestion);

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
*/

    /*private static void checkPermission(Question oldQuestion,User currectUser, User author, boolean enabled) {
        if (oldQuestion != null && oldQuestion.getAuthor() != null && !oldQuestion.getAuthor().equals(currectUser) &&
                !JwtTokenProvider.currentUserHasPermission("edit others question")) {
            throw new AuthException("permission denied: edit others question");
        }
        if ( ( ( oldQuestion != null && oldQuestion.getAuthor() != null && !oldQuestion.getAuthor().equals(author) ) || ( oldQuestion == null && !currectUser.equals(author) ))
                && !JwtTokenProvider.currentUserHasPermission("change author")) {
            throw new AuthException("permission denied: change author");
        }
        if (((oldQuestion != null && enabled != oldQuestion.isEnabled())
                ||(oldQuestion == null && enabled))
                && !JwtTokenProvider.currentUserHasPermission("enable and disable question")) {
            throw new AuthException("permission denied: enable and disable question");
        }
    }*/
    
}
