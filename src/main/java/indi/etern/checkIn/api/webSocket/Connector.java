package indi.etern.checkIn.api.webSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.api.webSocket.action.*;
import indi.etern.checkIn.api.webSocket.action.question.AddPartitionAction;
import indi.etern.checkIn.api.webSocket.action.question.DeletePartitionAction;
import indi.etern.checkIn.api.webSocket.action.question.DeleteQuestionAction;
import indi.etern.checkIn.api.webSocket.action.question.EditPartitionNameAction;
import indi.etern.checkIn.api.webSocket.action.setting.SaveSettingAction;
import indi.etern.checkIn.api.webSocket.action.traffic.GetTrafficByDateAction;
import indi.etern.checkIn.api.webSocket.action.user.*;
import indi.etern.checkIn.auth.JwtTokenProvider;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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
        logger.info("sid_" + sid + ":connected");
    }
    
    @OnClose
    public void onClose() {
        CONNECTORS.remove(this);
        subOnlineCount();
        logger.info("sid_" + sid + ":close");
//        releaseResource();
        if (sessionUser != null) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "userOffline");
            dataMap.put("qq", String.valueOf(sessionUser.getQQNumber()));
            webSocketService.sendMessageToAll(gson.toJson(dataMap));
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Map<String, Object> contentMap = gson.fromJson(message, HashMap.class);
            if (checkToken(contentMap)) return;//TODO TIP
            /*if (!JwtTokenProvider.currentUserHasPermission((String) contentMap.get("type"), PermissionType.WEB_SOCKET)) {
                sendError("no permission");
                return;
            }*/
            long qqInContentMap = 0;
            String qqStr = "";
            Object qqObject = contentMap.get("QQ");
            if (qqObject != null) {
                qqStr = (String) qqObject;
                qqInContentMap = Long.parseLong(qqStr);
            }
            boolean logging = true;
            switch ((String) contentMap.get("type")) {
                case "addPartition" -> {
                    final String partitionName = (String) contentMap.get("partitionName");
                    logging = doAction(new AddPartitionAction(partitionName));
                }
                case "deletePartition" -> {
                    final int partitionId = Integer.parseInt((String) contentMap.get("partitionId"));
                    logging = doAction(new DeletePartitionAction(partitionId));
                }
                case "deleteQuestion" -> {
                    final String questionMD5 = (String) contentMap.get("questionMD5");
                    logging = doAction(new DeleteQuestionAction(questionMD5));
                }
                case "editPartition" -> {
                    logging = doAction(new EditPartitionNameAction(Integer.parseInt((String) contentMap.get("partitionId")),(String) contentMap.get("partitionName")));
                }
                case "newUser" -> {
                    logging = doAction(new CreateUserAction(qqInContentMap, (String) contentMap.get("name"), (String) contentMap.get("role")));
                }
                case "deleteUser" -> {
                    if (qqInContentMap != sessionUser.getQQNumber() && webSocketService.isOnline(String.valueOf(qqInContentMap))) {
                        sendError("user is online");
                        return;
                    }
                    logging = doAction(new DeleteUserAction(qqInContentMap,sessionUser));
                }
                case "changeUserName" -> {
                    logging = doAction(new ChangeUserNameAction(qqInContentMap, (String) contentMap.get("newName")));
                }
                case "changePassword" -> {
                    logging = doAction(new ChangeUserPasswordAction(qqInContentMap,(String) contentMap.get("oldPassword"), (String) contentMap.get("newPassword")));
                }
                case "enableUser" -> {
                    logging = doAction(new SetUserStateAction(qqInContentMap,true));
                }
                case "disableUser" -> {
                    logging = doAction(new SetUserStateAction(qqInContentMap,false));
                }
                case "offLine" -> {
                    logging = doAction(new ForceOfflineAction(qqInContentMap));
                }
                case "changeRole" -> {
                    logging = doAction(new ChangeUserRoleAction(qqInContentMap, (String) contentMap.get("role")));
                }
                case "savePermission" -> {
                    Role role = roleService.findById((String) contentMap.get("role")).orElseThrow();
                    logging = doAction(new SavePermissionAction(role,(List<String>)contentMap.get("enable")));
                }
                case "createRole" -> {
                    logging = doAction(new CreateRoleAction((String) contentMap.get("role"),(List<String>)contentMap.get("enable")));
                }
                case "deleteRole" -> {
                    logging = doAction(new DeleteRoleAction((String) contentMap.get("role")));
                }
                case "saveSetting_examSetting" -> {
                    Map<String,Object> dataMap = (Map<String, Object>) contentMap.get("data");
                    logging = doAction(new SaveSettingAction(dataMap,"examSetting"));
                }
                case "saveSetting_checkingSetting" -> {
                    Map<String,Object> dataMap = (Map<String, Object>) contentMap.get("data");
                    logging = doAction(new SaveSettingAction(dataMap,"checkingSetting"));
                }
                case "getDateTrafficDetail" -> {
                    logging = doAction(new GetTrafficByDateAction(String.valueOf(contentMap.get("date"))));
                }
            }
            if (logging) {
                logger.info("sid_" + sid + ":" + message);
            }
        } catch (Exception e) {
            try {
                logger.error(e.getClass().getName() + ":" + e.getMessage());
                sendError(e.getClass().getSimpleName() + ":" + e.getMessage());
            } catch (IOException exception) {
                logger.error("while sending message:" + message + "to sid_" + sid + ":" + exception.getMessage());
            }
            e.printStackTrace();
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
        dataMap.put("message", data);
        sendMessage(gson.toJson(dataMap));
    }
    
    private boolean checkToken(Map<String, Object> contentMap) throws IOException {
        if (contentMap.get("type").equals("token")) {
            token = (String) contentMap.get("token");
            sessionUser = jwtTokenProvider.getUser(token);
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "userOnline");
            dataMap.put("qq", String.valueOf(sessionUser.getQQNumber()));
            webSocketService.sendMessageToAll(gson.toJson(dataMap));
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
        } else if (sessionUser != null) {
            if (sessionUser.isEnabled()) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        sessionUser,
                        null,
                        sessionUser.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            return !sessionUser.isEnabled();
        } else {
            return true;
        }
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(session.getBasicRemote().toString());
        error.printStackTrace();
    }
    
    public void sendMessage(JsonObject jsonObject) throws IOException {
        sendMessage(jsonObject.toString());
    }
    
    public void sendMessageWithOutLog(JsonObject jsonObject) throws IOException {
        sendMessageWithOutLog(jsonObject.toString());
    }
    
    public void sendMessageWithOutLog(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
    public void sendMessage(String message) throws IOException {
        logger.info("webSocket to sid_" + sid + ":" + message);
        this.session.getBasicRemote().sendText(message);
    }
    
    @NonNull
    @Override
    public List<String> getSubProtocols() {
        return new ArrayList<>();
    }
    
    public boolean isOpen() {
        return session.isOpen();
    }
    
    private boolean doAction(JsonResultAction jsonResultAction) throws Exception {
        Optional<JsonObject> optionalResult = jsonResultAction.call();
        if (optionalResult.isPresent()) {
            final JsonObject jsonObject = optionalResult.get();
            sendMessage(gson.toJson(jsonObject));
            if (!jsonObject.get("type").getAsString().equals("error")) {
                jsonResultAction.afterAction();
            }
        }
        return jsonResultAction.shouldLogging();
    }
}
