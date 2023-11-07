package indi.etern.checkIn.controller.html;

import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionFactory;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Manage {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    UserService userService;
    @Autowired
    PartitionService partitionService;
    @Autowired
    MultiPartitionableQuestionService multiPartitionableQuestionService;
    
    public Manage() {
//        System.out.println("Manage");
    }
    
    private boolean checkLoginCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        long qq = 0;
        String password = null;
        for (Cookie cookie : cookies) {
            switch (cookie.getName()) {
                case "password":
                    password = cookie.getValue();
                    break;
                case "qq":
                    try {
                        qq = Long.parseLong(cookie.getValue());
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return userService.check(qq, password);
    }
    
    @GetMapping("/manage/")
    public ModelAndView manage(HttpServletRequest request) {
        if (!checkLoginCookie(request)) {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("redirect:/login/");
            return mv;
        } else if (request.getParameter("page") == null) {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("manage/manage");
//        return mv;
            return mv;
        } else {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("manage/" + request.getParameter("pageClass") + "_" + request.getParameter("page"));//类似于"server_0" "user_0"
            return mv;
        }
    }
    
    @GetMapping("/manage/data")
    public String getDataOf(@RequestAttribute(name = "type") String type) {
        return "";
    }
    
    /*@GetMapping("/api/webSocket/{cid}/manage/newPartition")
    public void pushMessage(String message, @PathVariable String cid) {
        try {
            webSocketService.sendMessage(message, cid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
    
    @GetMapping("/login/")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("manage/login");
        return mv;
    }
    
    @RequestMapping(path = "/manage/page/{type}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView getPageOf(HttpServletRequest request, @PathVariable String type) {
        ModelAndView modelAndView = new ModelAndView("manage/pages/" + type);
        switch (type) {
            case "partitionQuestion" ->
                    modelAndView.addObject("partition", partitionService.findByName(request.getParameter("name")));
            case "newQuestion" -> {
                MultipleQuestionFactory multipleQuestionFactory = new MultipleQuestionFactory();
                MultiPartitionableQuestion multiPartitionableQuestion =
                        multipleQuestionFactory.setQuestionContent("")
                                .addChoice(new Choice("", true))
                                .addChoice(new Choice("", false))
                                .addPartition(Partition.getInstance("undefined"))
                                .build();
                request.setAttribute("question", multiPartitionableQuestion);
                modelAndView.addObject("question", multiPartitionableQuestion);
                modelAndView.addObject("isNew",Boolean.TRUE);
                modelAndView.setViewName("manage/pages/editQuestion");
            }
            case "editQuestion" -> {
                MultiPartitionableQuestion multiPartitionableQuestion = multiPartitionableQuestionService.getByMD5(request.getParameter("md5"));
                modelAndView.addObject("question", multiPartitionableQuestion);
                modelAndView.addObject("isNew",Boolean.FALSE);
            }
            case "partitionQuestionLeft" -> {
            
            }
        }
        return modelAndView;
    }
}
