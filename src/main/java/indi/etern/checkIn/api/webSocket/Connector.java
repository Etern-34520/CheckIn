package indi.etern.checkIn.api.webSocket;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.api.webSocket.action.partition.CreatePartitionAction;
import indi.etern.checkIn.api.webSocket.action.partition.DeletePartitionAction;
import indi.etern.checkIn.api.webSocket.action.partition.EditPartitionNameAction;
import indi.etern.checkIn.api.webSocket.action.partition.GetPartitions;
import indi.etern.checkIn.api.webSocket.action.question.*;
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
import io.micrometer.core.instrument.config.MeterFilter;
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
    public static final Logger logger = LoggerFactory.getLogger(Connector.class);
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
    private MeterFilter metricsHttpClientUriTagFilter;

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
        session.setMaxTextMessageBufferSize(8 * 1024 * 1024);//8MB
        session.setMaxBinaryMessageBufferSize(8 * 1024 * 1024);//8MB
        CONNECTORS.add(this);
        this.sid = sid;
        addOnlineCount();
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

    private final Map<String, PartMessage> partMessageMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    @OnMessage
    public boolean onMessage(String message) {
        try {
            Map<String, Object> contentMap = gson.fromJson(message, HashMap.class);
            if (checkToken(contentMap)) return false;//TODO TIP
            /*if (!JwtTokenProvider.currentUserHasPermission((String) contentMap.get("type"), PermissionType.WEB_SOCKET)) {
                sendError("no permission");
                return;
            }*/
            long qqInContentMap = 0;
            String qqStr = "";
            Object qqObject = contentMap.get("QQ");
            String messageId = contentMap.get("messageId").toString();
            if (qqObject != null) {
                qqStr = (String) qqObject;
                qqInContentMap = Long.parseLong(qqStr);
            }
            boolean logging = true;
            switch ((String) contentMap.get("type")) {
                case "partMessage" -> {
                    PartMessage partMessage;
                    String partId;
                    if (contentMap.get("messageIds") instanceof List<?> messageIds) {
                        partMessage = new PartMessage((List<String>) messageIds);
                        partMessageMap.put(messageId, partMessage);
                        partId = messageId;
                        sendMessage("{\"type\":\"success\",\"messageId\":\"" + messageId + "\"}");
                    } else if (contentMap.get("partId") instanceof String) {
                        partId = (String) contentMap.get("partId");
                        partMessage = partMessageMap.get(partId);
                        partMessage.put(messageId, (String) contentMap.get("messagePart"));
                        logging = false;
                    } else {
                        //TODO Tip
                        return false;
                    }
                    if (partMessage.isComplete()) {
                        logging = onMessage(partMessage.toString());
                        partMessageMap.remove(partId);
                    }
                }
                case "createPartition" -> {
                    final String partitionName = (String) contentMap.get("name");
                    logging = doAction(contentMap, new CreatePartitionAction(partitionName));
                }
                case "deletePartition" -> {
                    final int partitionId = (int) ((double) contentMap.get("id"));
                    logging = doAction(contentMap, new DeletePartitionAction(partitionId));
                }
                case "editPartition" -> {
                    logging = doAction(contentMap, new EditPartitionNameAction(Integer.parseInt((String) contentMap.get("id")), (String) contentMap.get("name")));
                }
                case "newUser" -> {
                    logging = doAction(contentMap, new CreateUserAction(qqInContentMap, (String) contentMap.get("name"), (String) contentMap.get("role")));
                }
                case "deleteUser" -> {
                    if (qqInContentMap != sessionUser.getQQNumber() && webSocketService.isOnline(String.valueOf(qqInContentMap))) {
                        sendError(messageId, "user is online");
                        return true;
                    }
                    logging = doAction(contentMap, new DeleteUserAction(qqInContentMap, sessionUser));
                }
                case "changeUserName" -> {
                    logging = doAction(contentMap, new ChangeUserNameAction(qqInContentMap, (String) contentMap.get("newName")));
                }
                case "changePassword" -> {
                    logging = doAction(contentMap, new ChangeUserPasswordAction(qqInContentMap, (String) contentMap.get("oldPassword"), (String) contentMap.get("newPassword")));
                }
                case "enableUser" -> {
                    logging = doAction(contentMap, new SetUserStateAction(qqInContentMap, true));
                }
                case "disableUser" -> {
                    logging = doAction(contentMap, new SetUserStateAction(qqInContentMap, false));
                }
                case "offLine" -> {
                    logging = doAction(contentMap, new ForceOfflineAction(qqInContentMap));
                }
                case "changeRole" -> {
                    logging = doAction(contentMap, new ChangeUserRoleAction(qqInContentMap, (String) contentMap.get("role")));
                }
                case "savePermission" -> {
                    Role role = roleService.findById((String) contentMap.get("role")).orElseThrow();
                    logging = doAction(contentMap, new SavePermissionAction(role, (List<String>) contentMap.get("enable")));
                }
                case "createRole" -> {
                    logging = doAction(contentMap, new CreateRoleAction((String) contentMap.get("role"), (List<String>) contentMap.get("enable")));
                }
                case "deleteRole" -> {
                    logging = doAction(contentMap, new DeleteRoleAction((String) contentMap.get("role")));
                }
                case "saveSetting" -> {
                    Map<String, Object> dataMap = (Map<String, Object>) contentMap.get("data");
                    logging = doAction(contentMap, new SaveSettingAction(dataMap, (String) contentMap.get("settingName")));
                }
                case "getDateTrafficDetail" -> {
                    logging = doAction(contentMap, new GetTrafficByDateAction(String.valueOf(contentMap.get("date"))));
                }
                case "getQuestionIdAndContentList" -> {
                    logging = doAction(contentMap, new GetQuestionIdAndContentListAction(Integer.parseInt(contentMap.get("partitionId").toString())));
                }
                case "getPartitions" -> {
                    logging = doAction(contentMap, new GetPartitions());
                }
                case "getQuestionInfo" -> {
                    logging = doAction(contentMap, new GetQuestionInfoAction(contentMap.get("questionId").toString()));
                }
                case "getQuestionInfos" -> {
                    logging = doAction(contentMap, new GetQuestionInfosAction(((List<String>)contentMap.get("questionIds"))));
                }
                case "getUsers" -> {
                    logging = doAction(contentMap, new GetAllUserAction());
                }
                case "updateQuestions" -> {
                    logging = doAction(contentMap, new UpdateQuestionsAction((List<Object>) contentMap.get("updatedQuestions"), sessionUser.getQQNumber()));
                    Object o = contentMap.get("deletedQuestionIds");
                    if (o instanceof List<?>) {
                        List<String> deletedQuestionIds = (List<String>) o;
                        if (!deletedQuestionIds.isEmpty())
                            logging = logging & doAction(contentMap, new DeleteQuestionsAction(deletedQuestionIds));
                    }
                }
                case "upVote" -> {
                    logging = doAction(contentMap, new UpVoteAction(contentMap.get("questionId").toString(), sessionUser));
                }
                case "downVote" -> {
                    logging = doAction(contentMap, new DownVoteAction(contentMap.get("questionId").toString(), sessionUser));
                }
                case "restoreVote" -> {
                    logging = doAction(contentMap, new RestoreVoteAction(contentMap.get("questionId").toString(), sessionUser));
                }
            }
            if (logging) {
                logger.info("{}({}):{}", sessionUser.getName(), sid, message);
            }
            return logging;
        } catch (Exception e) {
            try {
                if (message.length() < 65535) {
                    logger.info("{}({}):{}", sessionUser.getName(), sid, message);
                }
                logger.error("{}:{}", e.getClass().getName(), e.getMessage());
                String messageId = gson.fromJson(message, HashMap.class).get("messageId").toString();
                sendError(messageId, e.getClass().getSimpleName() + ":" + e.getMessage());
            } catch (IOException exception) {
                logger.error("while sending message:{}to {}({}):{}", message, sessionUser.getName(), sid, exception.getMessage());
            }
            e.printStackTrace();
            return true;
        }
    }

    private void sendUpdateUser(User user) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "updateUser");
        dataMap.put("user", user.toDataMap());
        webSocketService.sendMessageToAll(gson.toJson(dataMap));
    }

    private void sendUserIsOnlineError() throws IOException {
        /*sendError(messageId,"user is online");*/
    }

    private void sendError(String messageId, String data) throws IOException {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("type", "error");
        dataMap.put("message", data);
        dataMap.put("messageId", messageId);
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

            JsonObject message = new JsonObject();
            message.addProperty("messageId", contentMap.get("messageId").toString());
            JsonArray permissions = new JsonArray();
            sessionUser.getRole().getPermissions().forEach(permission -> {
                permissions.add(permission.getName());
            });
            message.add("permissions", permissions);
            sendMessage(gson.toJson(message));
            return true;
        } else if (token == null || token.isEmpty()) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "error");
            dataMap.put("data", "token is empty");
            dataMap.put("messageId", contentMap.get("messageId").toString());
            sendMessage(gson.toJson(dataMap));
            return true;
        } else if (!jwtTokenProvider.validateToken(token)) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "error");
            dataMap.put("data", "token is invalid");
            dataMap.put("messageId", contentMap.get("messageId").toString());
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
            if (!sessionUser.isEnabled()) {
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("type", "error");
                dataMap.put("data", "user is disabled");
                dataMap.put("messageId", contentMap.get("messageId").toString());
                sendMessage(gson.toJson(dataMap));
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
        logger.info("webSocket to {}({}):{}", sessionUser.getName(), sid, message);
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

    private boolean doAction(Map<String, Object> contentMap, JsonResultAction jsonResultAction) throws Exception {
        Optional<JsonObject> optionalResult = jsonResultAction.call();
        if (contentMap.get("expectResponse") != null && !((boolean) contentMap.get("expectResponse"))) {
            return jsonResultAction.shouldLogging();
        }
        JsonObject jsonObject;
        jsonObject = optionalResult.orElseGet(JsonObject::new);
        jsonObject.addProperty("messageId", contentMap.get("messageId").toString());
        sendMessageWithOutLog(jsonObject);
        {
            Optional<JsonObject> message = jsonResultAction.logMessage(optionalResult);
            if (message.isPresent()) {
                JsonObject jsonObject1 = message.get();
                jsonObject1.addProperty("messageId", contentMap.get("messageId").toString());
                if (jsonResultAction.shouldLogging())
                    logger.info("webSocket to {}({}):{}", sessionUser.getName(), sid, jsonObject1);
            }
            if (jsonObject.get("type") != null && !jsonObject.get("type").getAsString().equals("error")) {
                jsonResultAction.afterAction();
            }
        }
        return jsonResultAction.shouldLogging();
    }

}
