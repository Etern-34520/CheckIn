package indi.etern.checkIn.controller.html;

import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.*;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class Manage {
    final WebSocketService webSocketService;
    final UserService userService;
    final UserTrafficService userTrafficService;
    final PartitionService partitionService;
    final RoleService roleService;
    final MultiPartitionableQuestionService multiPartitionableQuestionService;
    
    public Manage(WebSocketService webSocketService, UserService userService, UserTrafficService userTrafficService, PartitionService partitionService, RoleService roleService, MultiPartitionableQuestionService multiPartitionableQuestionService) {
//        System.out.println("Manage");
        this.webSocketService = webSocketService;
        this.userService = userService;
        this.userTrafficService = userTrafficService;
        this.partitionService = partitionService;
        this.roleService = roleService;
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }
    
    private static void newQuestion(HttpServletRequest request, ModelAndView modelAndView) {
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
                        .setId(UUID.randomUUID().toString())
                        .setAuthor(user)
                        .setEnable(false)
//                        .setEnable(Boolean.parseBoolean(request.getParameter("enabled")))
                        .build();
//        request.setAttribute("question", multiPartitionableQuestion);
        modelAndView.addObject("question", multiPartitionableQuestion);
        modelAndView.setViewName("manage/pages/editQuestion");
    }
    
    @RequestMapping("/manage/")
//    @PreAuthorize("jwtTokenProvider.currentUserHasPermission('manage user')")
    public ModelAndView manage(HttpServletRequest request) {
        final ModelAndView modelAndView = new ModelAndView();
        addPermissionsBoolean(modelAndView);
        if (request.getParameter("page") == null) {
            modelAndView.setViewName("manage/manage");
        } else {
            modelAndView.setViewName("manage/" + request.getParameter("pageClass") + "_" + request.getParameter("page"));//类似于"server_0" "user_0"
        }
        return modelAndView;
    }
    
    @RequestMapping("/login/")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("manage/login");
        return mv;
    }
    
    @RequestMapping(path = "/manage/page/{type}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView getPageOf(HttpServletRequest request, @PathVariable String type) throws ServletException, IOException {
        ModelAndView modelAndView = new ModelAndView("manage/pages/" + type);
        addPermissionsBoolean(modelAndView);
        switch (type) {
            case "partitionQuestion" -> partitionQuestion(request, modelAndView);
            case "newQuestion" -> newQuestion(request, modelAndView);
            case "editQuestion" -> editQuestion(request, modelAndView);
            case "questionFormOfFormData" -> {
                MultiPartitionableQuestion question = MultiPartitionableQuestionService.buildQuestionFromRequest(request, null, null);
                modelAndView.addObject("question", question);
                modelAndView.addObject("ignorePartitionSelection", false);
                modelAndView.setViewName("manage/pages/editQuestion");
            }
            case "userInfo" -> userInfo(request, modelAndView);
            case "userPane" -> {
                modelAndView.addObject("webSocketService", webSocketService);
                modelAndView.addObject("user", userService.findByQQNumber(Long.parseLong(request.getParameter("QQ"))).orElseThrow());
            }
            case "changeRole" ->
                    modelAndView.addObject("user", userService.findByQQNumber(Long.parseLong(request.getParameter("QQ"))).orElseThrow());
            case "questionOverview" ->
                    modelAndView.addObject("question", multiPartitionableQuestionService.getById(request.getParameter("id")));
            case "editPermission" ->
                    modelAndView.addObject("role", roleService.findByType(request.getParameter("roleType")).orElseThrow());
            case "shrinkPane" -> {
                modelAndView.addObject("title", request.getParameter("title"));
                modelAndView.addObject("content", request.getParameter("content"));
                modelAndView.addObject("id", request.getParameter("id"));
                modelAndView.addObject("clazz", request.getParameter("clazz"));
            }
            case "trafficDetail" -> {
                modelAndView.addObject("userTraffic", userTrafficService.findById(Integer.parseInt(request.getParameter("id"))).orElseThrow());
            }
        }
        return modelAndView;
    }
    
    private void partitionQuestion(HttpServletRequest request, ModelAndView modelAndView) throws BadRequestException {
        final String sortBy = request.getParameter("sortBy");
        final String sortType = request.getParameter("sortType");
        final Partition partition = partitionService.findById(Integer.valueOf(request.getParameter("id"))).orElseThrow();
        if (sortBy != null && sortType != null) {
            Comparator<MultiPartitionableQuestion> comparator;
            final Comparator<MultiPartitionableQuestion> contentComparator = (o1, o2) -> {
                int compare = o1.getContent().compareTo(o2.getContent());
                /*if (compare == 0) {
                    compare = 1;
                }*/
                return compare;
            };
            final Comparator<MultiPartitionableQuestion> authorNameComparator = (o1, o2) -> {
                if (o1.getAuthor() == null) {
                    if (o2.getAuthor() == null) {
                        return 0;//0
                    } else {
                        return -1;
                    }
                } else {
                    if (o2.getAuthor() == null) {
                        return 1;
                    } else {
                        int compare = o1.getAuthor().getName().compareTo(o2.getAuthor().getName());
//                        if (compare == 0) {
//                            compare = 1;
//                        }
                        return compare;
                    }
                }
            };
            final Comparator<MultiPartitionableQuestion> editTimeComparator = (o1, o2) -> {
                final LocalDateTime o1Time = o1.getLastEditTime();
                final LocalDateTime o2Time = o2.getLastEditTime();
                if (o1Time == null) {
                    if (o2Time == null) {
                        return 0;//0
                    } else {
                        return -1;
                    }
                } else {
                    if (o2Time == null) {
                        return 1;
                    } else if (o1Time.isBefore(o2Time)) {
                        return -1;
                    } else if (o1Time.isEqual(o2Time)) {
                        return 0;//0
                    } else {
                        return 1;
                    }
                }
            };
            switch (sortBy) {
                case "content" -> comparator = contentComparator
                        .thenComparing(editTimeComparator)
                        .thenComparing(authorNameComparator)
                        .thenComparing((o1, o2) -> 1);
                case "author" -> comparator = authorNameComparator
                        .thenComparing(contentComparator)
                        .thenComparing(editTimeComparator)
                        .thenComparing((o1, o2) -> 1);
                case "lastEditTime" -> comparator = editTimeComparator
                        .thenComparing(contentComparator)
                        .thenComparing(authorNameComparator)
                        .thenComparing((o1, o2) -> 1);
                default -> {
                    throw new BadRequestException("parameter \"sortBy\" must be \"content\",\"author\"or\"lastEditTime\"");
                }
            }
            if (sortType.equalsIgnoreCase("desc")) {
                comparator = comparator.reversed();
            } else if (!sortType.equalsIgnoreCase("asc")) {
                throw new BadRequestException("parameter \"sortType\" must be \"asc\"or\"desc\"");
            }
            partition.setSort(comparator);
        }
        modelAndView.addObject("partition", partition);
    }
    
    private void addPermissionsBoolean(ModelAndView modelAndView) {
        Map<String,Boolean> permissionMap = new HashMap<>();
        for (Permission permission : roleService.findAllPermission()) {
            permissionMap.put("permission_" + permission.getName().replace(' ', '_'),JwtTokenProvider.currentUserHasPermission(permission.getName()));
//            modelAndView.addObject("permission_" + permission.getName().replace(' ', '_'), JwtTokenProvider.currentUserHasPermission(permission.getName()));
        }
        modelAndView.addObject("permissionMap",permissionMap);
    }
    
    private void userInfo(HttpServletRequest request, ModelAndView modelAndView) {
        final String qqString = request.getParameter("QQ");
        final long qq = Long.parseLong(qqString);
        Optional<User> optionalUser = userService.findByQQNumber(qq);
        if (optionalUser.isPresent()) {
            modelAndView.addObject("user1", optionalUser.get());
        } else {
            throw new NoSuchElementException("user with qq:" + qqString);
        }
    }
    
    private void editQuestion(HttpServletRequest request, ModelAndView modelAndView) {
        MultiPartitionableQuestion multiPartitionableQuestion = multiPartitionableQuestionService.getById(request.getParameter("id"));
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
            Object objUser = request.getAttribute("currentUser");
            User user;
            if (objUser instanceof User user1) {
                user = user1;
            } else {
                user = User.exampleOfName("unknown");
            }
            MultipleQuestionBuilder multipleQuestionFactory = new MultipleQuestionBuilder();
            multiPartitionableQuestion =
                    multipleQuestionFactory.setQuestionContent("")
                            .addChoice(new Choice("", true))
                            .addChoice(new Choice("", false))
                            .addPartition(partition)
                            .setId(request.getParameter("id"))
                            .setAuthor(user)
                            .build();
//                    request.setAttribute("question", multiPartitionableQuestion);
            modelAndView.addObject("question", multiPartitionableQuestion);
        }
    }
}
