package indi.etern.checkIn.controller.html;

import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    
    private static void newQuestion(HttpServletRequest request, ModelAndView modelAndView) {
        
        //生成时间戳md5
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String timeStamp = df.format(new Date());
        MessageDigest md5Digest = null;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
        assert md5Digest != null;
        md5Digest.update(timeStamp.getBytes());
        byte[] byteArray = md5Digest.digest();
        
        BigInteger bigInt = new BigInteger(1, byteArray);
        StringBuilder result = new StringBuilder(bigInt.toString(16));
        while (result.length() < 32) {
            result.insert(0, "0");
        }
        
        //构建样本题目
        MultipleQuestionBuilder multipleQuestionFactory = new MultipleQuestionBuilder();
        final String partitionIdString = request.getParameter("partitionId");
        Partition partition;
        if (partitionIdString == null) {
            partition = Partition.getInstance("undefined");
            modelAndView.addObject("ignorePartitionSelection", Boolean.TRUE);
        } else {
            partition = Partition.getInstance(Integer.parseInt(partitionIdString));
            modelAndView.addObject("ignorePartitionSelection", Boolean.FALSE);
        }
        if (request.getParameter("ignorePartitionSelection") != null)
            if (request.getParameter("ignorePartitionSelection").equals("true")) {
                modelAndView.addObject("ignorePartitionSelection", Boolean.TRUE);
            } else {
                modelAndView.addObject("ignorePartitionSelection", Boolean.FALSE);
            }
        Object objUser = request.getAttribute("currentUser");
        User user;
        if (objUser instanceof User user1) {
            user = user1;
        } else {
            user = User.exampleOfName("unknown");
        }
        MultiPartitionableQuestion multiPartitionableQuestion =
                multipleQuestionFactory.setQuestionContent("")
                        .addChoice(new Choice("", true))
                        .addChoice(new Choice("", false))
                        .addPartition(partition)
                        .setMD5(result.toString())
                        .setAuthor(user)//TODO 默认当前用户
                        .setEnable(false)
//                        .setEnable(Boolean.parseBoolean(request.getParameter("enabled")))
                        .build();
//        request.setAttribute("question", multiPartitionableQuestion);
        modelAndView.addObject("question", multiPartitionableQuestion);
        modelAndView.setViewName("manage/pages/editQuestion");
    }
    
    private boolean checkLoginCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        long qq = 0;
        String password = null;
        try {
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
        } catch (NullPointerException e) {
            return false;
        }
        return userService.check(qq, password);
    }
    
    @RequestMapping("/manage/")
    public ModelAndView manage(HttpServletRequest request) {
        if (request.getParameter("page") == null) {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("manage/manage");
            return mv;
        } else {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("manage/" + request.getParameter("pageClass") + "_" + request.getParameter("page"));//类似于"server_0" "user_0"
            return mv;
        }
    }
    
    @RequestMapping("/login/")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("manage/login");
        return mv;
    }
    
    @RequestMapping(path = "/manage/page/{type}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView getPageOf(HttpServletRequest request, @PathVariable String type) {
        ModelAndView modelAndView = new ModelAndView("manage/pages/" + type);
//        modelAndView.addObject("ignorePartitionSelection",Boolean.FALSE);
        switch (type) {
            case "partitionQuestion" ->
                    modelAndView.addObject("partition", partitionService.findById(Integer.valueOf(request.getParameter("id"))).orElseThrow());
            case "newQuestion" -> newQuestion(request, modelAndView);
            case "editQuestion" -> editQuestion(request, modelAndView);
//            case "partitionQuestionLeft" -> {}
            case "userInfo" -> userInfo(request,modelAndView);
            case "userPane" -> {
                modelAndView.addObject("webSocketService",webSocketService);
                modelAndView.addObject("user",userService.findByQQNumber(Long.parseLong(request.getParameter("QQ"))).orElseThrow());
            }
            case "changeRole" -> modelAndView.addObject("user",userService.findByQQNumber(Long.parseLong(request.getParameter("QQ"))).orElseThrow());
            case "questionOverview" -> modelAndView.addObject("question",multiPartitionableQuestionService.getByMD5(request.getParameter("md5")));
        }
        return modelAndView;
    }
    
    private void userInfo(HttpServletRequest request, ModelAndView modelAndView) {
        final String qqString = request.getParameter("QQ");
        final long qq = Long.parseLong(qqString);
        Optional<User> optionalUser = userService.findByQQNumber(qq);
        if (optionalUser.isPresent()) {
            modelAndView.addObject("user1",optionalUser.get());
        } else {
            throw new NoSuchElementException("user with qq:"+qqString);
        }
    }
    
    private void editQuestion(HttpServletRequest request, ModelAndView modelAndView) {
        MultiPartitionableQuestion multiPartitionableQuestion = multiPartitionableQuestionService.getByMD5(request.getParameter("md5"));
        modelAndView.addObject("newQuestion", Boolean.FALSE);
        if (multiPartitionableQuestion != null) {
            modelAndView.addObject("question", multiPartitionableQuestion);
            modelAndView.addObject("ignorePartitionSelection", Boolean.FALSE);
        } else {
            String partitionIdString = request.getParameter("partitionId");
            Partition partition;
            if (partitionIdString == null) {
                partition = Partition.getInstance("undefined");
                final String ignorePartitionSelection = request.getParameter("ignorePartitionSelection");
                if (ignorePartitionSelection == null || !ignorePartitionSelection.equals("true")) {
                    modelAndView.addObject("ignorePartitionSelection", Boolean.TRUE);
                } else {
                    modelAndView.addObject("ignorePartitionSelection", Boolean.FALSE);
                }
            } else {
                partition = Partition.getInstance(Integer.parseInt(partitionIdString));
                modelAndView.addObject("ignorePartitionSelection", Boolean.FALSE);
            }
            MultipleQuestionBuilder multipleQuestionFactory = new MultipleQuestionBuilder();
            multiPartitionableQuestion =
                    multipleQuestionFactory.setQuestionContent("")
                            .addChoice(new Choice("", true))
                            .addChoice(new Choice("", false))
                            .addPartition(partition)
                            .setMD5(request.getParameter("md5"))
                            .setAuthor(User.exampleOfName("unknown"))//TODO 默认当前用户
                            .build();
//                    request.setAttribute("question", multiPartitionableQuestion);
            modelAndView.addObject("question", multiPartitionableQuestion);
        }
    }
}
