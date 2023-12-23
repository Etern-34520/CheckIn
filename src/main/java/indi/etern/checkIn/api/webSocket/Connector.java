package indi.etern.checkIn.api.webSocket;

import com.google.gson.Gson;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.SubProtocolCapable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/api/websocket/{sid}")
public class Connector implements SubProtocolCapable {
    public static final HashSet<String> ALL = new HashSet<>(0);
    public static final CopyOnWriteArraySet<Connector> CONNECTORS = new CopyOnWriteArraySet<>();
    private static final Logger logger = LoggerFactory.getLogger(CheckInApplication.class);
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static PartitionService partitionService;
    private static WebSocketService webSocketService;
    private static UserService userService;
    private static MultiPartitionableQuestionService multiPartitionableQuestionService;
    private static TransactionTemplate transactionTemplate;
    private static JwtTokenProvider jwtTokenProvider;
    private static Gson gson;
    private static RoleService roleService;
    private String token;
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收sid
    @Getter
    private String sid = "";
    private User sessionUser;
    
    public Connector() {
//        this.partitionService = CheckInApplication.applicationContext.getBean(PartitionService.class);
    }
    
    public static int getOnlineCount() {
        return onlineCount.get();
    }
    
    public static void addOnlineCount() {
        onlineCount.getAndIncrement();
    }
    
    public static void subOnlineCount() {
        onlineCount.getAndDecrement();
    }
    
    @Autowired
    public void setPartitionService(PartitionService partitionService) {
        Connector.partitionService = partitionService;
    }
    
    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        Connector.webSocketService = webSocketService;
    }
    
    @Autowired
    public void setUserService(UserService userService) {
        Connector.userService = userService;
    }
    
    @Autowired
    public void setRoleService(RoleService roleService) {
        Connector.roleService = roleService;
    }
    
    @Autowired
    public void setMultiPartitionableQuestionService(MultiPartitionableQuestionService multiPartitionableQuestionService) {
        Connector.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }
    
    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        Connector.transactionTemplate = transactionTemplate;
    }
    
    @Autowired
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        Connector.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Autowired
    public void setGson(Gson gson) {
        Connector.gson = gson;
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        CONNECTORS.add(this);     // 加入set中
        this.sid = sid;
        addOnlineCount();           // 在线数加1
        //            sendMessage("conn_success");
        logger.info("sid_" + sid + ":connected");
    }
    
    @OnClose
    public void onClose() {
        CONNECTORS.remove(this);  // 从set中删除
        subOnlineCount();              // 在线数减1
        // 断开连接情况下，更新主板占用情况为释放
        logger.info("sid_" + sid + ":close");
        releaseResource();
    }
    
    private void releaseResource() {
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            logger.info("sid_" + sid + ":" + message);
            Map<String, Object> contentMap = gson.fromJson(message, HashMap.class);
            final String partitionName = (String) contentMap.get("partitionName");
            if (checkToken(contentMap)) return;//TODO TIP
            
            long qqInContentMap = 0;
            String qqStr = "";
            Object qqObject = contentMap.get("QQ");
            if (qqObject != null) {
                qqStr = (String) qqObject;
                qqInContentMap = Long.parseLong(qqStr);
            }
            switch ((String) contentMap.get("type")) {
//                case "token" -> token = (String) contentMap.get("token");
                case "addPartition" -> {
                    final Partition partition = Partition.getInstance(partitionName);
                    partitionService.save(partition);
                    sendMessage("{\"type\":\"addPartitionCallBack\",\"id\":" + partition.getId() + "}");
                    sendUpdatePartitionToAll();
                }
                case "deletePartition" -> transactionTemplate.execute((TransactionCallback<Object>) result -> {
                    try {
                        Partition partition = partitionService.findByName(partitionName);
                        if (partition.getQuestions().isEmpty()) {
                            partitionService.delete(partition);
                            sendUpdatePartitionToAll();
                        } else {
                            sendError("partition " + partitionName + " is not empty");
                        }
                    } catch (Exception e) {
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                });
                case "deleteQuestion" -> {
                    final String questionMD5 = (String) contentMap.get("questionMD5");
                    multiPartitionableQuestionService.unbindAndDeleteById(questionMD5);
//                    multiPartitionableQuestionService.deleteById(questionMD5);
                    sendDeleteQuestionToAll(questionMD5);
                }
                case "editPartition" -> {
                    final String newName = (String) contentMap.get("partitionName");
                    Partition partition = partitionService.findById(Integer.valueOf(contentMap.get("partitionId").toString())).orElseThrow();
                    partition.setName(newName);
                    partitionService.saveAndFlush(partition);
                    sendUpdatePartitionToAll();
                }
                case "newUser" -> {
                    final long qqNumber = Long.parseLong(contentMap.get("qq").toString());
                    if (userService.existsByQQNumber(qqNumber)) {
                        sendError("user already exists");
//                        dataMap.put("reason", "user already exists");
                    } else {
                        final String initPassword = UUID.randomUUID().toString();
                        User newUser = new User((String) contentMap.get("name"), qqNumber, initPassword);
                        newUser.setRole(Role.getInstance(contentMap.get("role").toString()));
                        userService.save(newUser);
                        {
                            Map<String, String> dataMap = new HashMap<>();
                            dataMap.put("type", "success");
                            dataMap.put("initPassword", initPassword);
                            sendMessage(gson.toJson(dataMap));
                        }
                        {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("type", "addUser");
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", newUser.getName());
                            userMap.put("qq", newUser.getQQNumber());
                            userMap.put("role", newUser.getRole().getType());
                            dataMap.put("user", userMap);
                            webSocketService.sendMessageToAll(gson.toJson(dataMap));
                        }
                    }
                }
                case "deleteUser" -> {
                    final Optional<User> optionalUser = userService.findByQQNumber(qqInContentMap);
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        if (!user.equals(sessionUser) && webSocketService.isOnline(qqStr)) {
                            sendUserIsOnlineError();
                            return;
                        }
                        userService.unbindAndDelete(user);
                        {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("type", "deleteUser");
                            dataMap.put("QQ", qqInContentMap);
                            webSocketService.sendMessageToAll(gson.toJson(dataMap));
                        }
                    }
                }
                case "changeUserName" -> {
                    final String newUserName = (String) contentMap.get("newName");
                    if (newUserName != null) {
                        sessionUser.setName(newUserName);
                        userService.save(sessionUser);
                        sendUpdateUser(sessionUser);
                    } else
                        sendError("newNameIsNull");
                }
                case "changePassword" -> {
                    if (sessionUser.getPassword().equals(contentMap.get("oldPassword"))) {
                        sessionUser.setPassword((String) contentMap.get("newPassword"));
                        userService.save(sessionUser);
                        sendMessage("{\"type\":\"success\"}");
                    } else {
                        sendMessage("{\"type\":\"error\"}");
                    }
                }
                case "enableUser" -> {
                    final Optional<User> optionalUser = userService.findByQQNumber(qqInContentMap);
                    if (optionalUser.isPresent()) {
                        final User user = optionalUser.get();
                        if (webSocketService.isOnline(user)) {
                            sendUserIsOnlineError();
                        } else {
                            user.setEnabled(true);
                            userService.save(user);
                            sendUpdateUser(user);
                        }
                    }
//                    userService.enableByQQNumber(qqInContentMap);
                }
                case "disableUser" -> {
                    final Optional<User> optionalUser = userService.findByQQNumber(qqInContentMap);
                    if (optionalUser.isPresent()) {
                        final User user = optionalUser.get();
                        if (webSocketService.isOnline(user)) {
                            sendUserIsOnlineError();
                        } else {
                            user.setEnabled(false);
                            userService.save(user);
                            sendUpdateUser(user);
                        }
                    }
//                    userService.disableByQQNumber(qqInContentMap);
                }
                case "offLine" -> webSocketService.sendMessage("{\"type\":\"offLine\"}", qqStr);
                case "changeRole" -> {
                    if (userService.existsByQQNumber(qqInContentMap)) {
                        User user = userService.changeRoleById(qqInContentMap, contentMap.get("role").toString());
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put("type", "success");
                        sendMessage(gson.toJson(dataMap));
                        sendUpdateUser(user);
                    } else {
                        sendError("user not found");
                    }
                }
            }
        } catch (Exception e) {
            try {
                logger.error(e.getClass().getName() + ":" + e.getMessage());
                sendError(e.getClass().getSimpleName() + ":" + e.getMessage());
            } catch (IOException exception) {
                logger.error("while sending message:" + message + "to sid_" + sid + ":" + exception.getMessage());
            }
        }
    }
    
    private void sendUpdateUser(User user) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "updateUser");
        dataMap.put("user", user.toDataMap());
        webSocketService.sendMessageToAll(gson.toJson(dataMap));
    }
    
    private void sendUserIsOnlineError() throws IOException {
        sendError("user is online");
    }
    
    private void sendError(String data) throws IOException {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("type", "error");
        dataMap.put("data", data);
        sendMessage(gson.toJson(dataMap));
    }
    
    private boolean checkToken(Map<String, Object> contentMap) throws IOException {
        if (contentMap.get("type").equals("token")) {
            token = (String) contentMap.get("token");
            sessionUser = jwtTokenProvider.getUser(token);
            return true;
        } else if (token == null || token.isEmpty()) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "error");
            dataMap.put("data", "token is empty");
            sendMessage(gson.toJson(dataMap));
            return true;
        } else if (!jwtTokenProvider.validateToken(token)) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "error");
            dataMap.put("data", "token is invalid");
            sendMessage(gson.toJson(dataMap));
            return true;
        } else return sessionUser != null && !sessionUser.isEnabled();
    }
    
    private void sendDeleteQuestionToAll(String questionMD5) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "deleteQuestion");
        dataMap.put("questionMD5", questionMD5);
        webSocketService.sendMessageToAll(gson.toJson(dataMap));
    }
    
    private void sendUpdatePartitionToAll() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "updatePartitionList");
        final List<Partition> partitions = partitionService.findAll();
        final Map<String, String> partitionIdNameMap = new HashMap<>();
        for (Partition partition : partitions) {
            partitionIdNameMap.put(String.valueOf(partition.getId()), partition.getName());
        }
        dataMap.put("partitionIdNameMap", partitionIdNameMap);
        webSocketService.sendMessageToAll(gson.toJson(dataMap));
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(session.getBasicRemote().toString());
        error.printStackTrace();
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
    @NonNull
    @Override
    public List<String> getSubProtocols() {
        return new ArrayList<>();
    }
}
