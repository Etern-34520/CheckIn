package indi.etern.checkIn.api.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.Result;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.SubProtocolCapable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/api/websocket/{sid}")
@ConditionalOnWebApplication
public class Connector implements SubProtocolCapable {
    public static final HashSet<String> ALL = new HashSet<>(0);
    public static final CopyOnWriteArraySet<Connector> CONNECTORS = new CopyOnWriteArraySet<>();
    public static final Logger logger = LoggerFactory.getLogger(Connector.class);
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static WebSocketService webSocketService;
    private static JwtTokenProvider jwtTokenProvider;
    private static ActionExecutor actionExecutor;
    private String token;
    private Session session;
    @Getter
    private String sid = "";
    private User sessionUser;
    private final ObjectMapper objectMapper = MVCConfig.getObjectMapper();
    
    public Connector() {
//        this.partitionService = CheckInApplication.applicationContext.getBean(PartitionService.class);
    }
    
    public static void addOnlineCount() {
        onlineCount.getAndIncrement();
    }
    
    public static void subOnlineCount() {
        onlineCount.getAndDecrement();
    }
    
    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        Connector.webSocketService = webSocketService;
    }
    
    @Autowired
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        Connector.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Autowired
    public void set(ActionExecutor actionExecutor) {
        Connector.actionExecutor = actionExecutor;
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
    
    @SneakyThrows
    @OnClose
    public void onClose() {
        CONNECTORS.remove(this);
        webSocketService.unsubscribeAllChannels(sid);
        subOnlineCount();
        logger.info("sid_" + sid + ":close");
//        releaseResource();
        if (sessionUser != null) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "userOffline");
            dataMap.put("qq", String.valueOf(sessionUser.getQQNumber()));
            webSocketService.sendMessageToAll(objectMapper.writeValueAsString(dataMap));
        }
    }
    
    private final Map<String, PartMessage> partMessageMap = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    @OnMessage
    public void onMessage(String message) {
        try {
            Map<String, Object> contentMap = objectMapper.readValue(message, HashMap.class);
            if (tokenIsInvalid(contentMap)) {
                return;//TODO
            }
            String messageId = contentMap.get("messageId").toString();
            switch (contentMap.get("type").toString()) {
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
                    } else {
                        sendMessage("{\"type\":\"error\",\"message\":\"not supported message format\",\"messageId\":\"" + messageId + "\"}");
                        return;
                    }
                    if (partMessage.isComplete()) {
                        onMessage(partMessage.toString());
                        partMessageMap.remove(partId);
                    }
                }
                case "subscribe" -> {
                    final String channelName = contentMap.get("channel").toString();
                    logger.info("websocket subscribe from {}({}): channel \"{}\"",sessionUser.getName(),sessionUser.getQQNumber(),channelName);
                    webSocketService.subscribeChannel(sid, channelName);
                    sendMessage("{\"type\":\"success\",\"messageId\":\"" + messageId + "\"}");
                }
                case "unsubscribe" -> {
                    final String channelName = contentMap.get("channel").toString();
                    logger.info("websocket unsubscribe from {}({}): channel \"{}\"",sessionUser.getName(),sessionUser.getQQNumber(),channelName);
                    webSocketService.unsubscribeChannel(sid, channelName);
                    sendMessage("{\"type\":\"success\",\"messageId\":\"" + messageId + "\"}");
                }
                default -> {
                    String logMessage = message.length() > 65535 ? message.substring(0, 4096) : message;
                    try {
                        Result result = actionExecutor.executeByMap(contentMap);
                        logger.info("{}({}):{}", sessionUser.getName(), sid, logMessage);
                        if (result.getResult().isPresent()) {
                            LinkedHashMap<String, Object> map = result.getResult().get();
                            map.put("messageId", messageId);
                            sendMessage(map);
                        }
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            e.printStackTrace();
                        }
                        logger.error("{} : {}", e.getClass().getName(), e.getMessage());
                        logger.info("Exception caused by message from {}({}):{}", sessionUser.getName(), sid, logMessage);
                        if (e instanceof PermissionDeniedException permissionDeniedException) {
                            sendError(messageId, permissionDeniedException.getDescription());
                        } else
                            sendError(messageId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            try {
                logger.error("{}:{}", e.getClass().getName(), e.getMessage());
                String messageId = objectMapper.readValue(message, HashMap.class).get("messageId").toString();
                sendError(messageId, e.getClass().getSimpleName() + ":" + e.getMessage());
            } catch (IOException exception) {
                logger.error("while sending message:{}to {}({}):{}", message, sessionUser.getName(), sid, exception.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    @SneakyThrows
    private void sendUpdateUser(User user) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "updateUser");
        dataMap.put("user", user.toDataMap());
        webSocketService.sendMessageToAll(objectMapper.writeValueAsString(dataMap));
    }
    
    private void sendError(String messageId, String data) throws IOException {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("type", "error");
        dataMap.put("message", data);
        dataMap.put("messageId", messageId);
        sendMessage(objectMapper.writeValueAsString(dataMap));
    }
    
    private boolean tokenIsInvalid(Map<String, Object> contentMap) throws IOException {
        if (contentMap.get("type").equals("token")) {
            token = (String) contentMap.get("token");
            sessionUser = jwtTokenProvider.getUser(token);
            if (!sid.equals(String.valueOf(sessionUser.getQQNumber()))) {
                sendError(contentMap.get("messageId").toString(), "sid is not equal to qq");
                return true;
            }
            
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "userOnline");
            dataMap.put("qq", String.valueOf(sessionUser.getQQNumber()));
            webSocketService.sendMessageToAll(objectMapper.writeValueAsString(dataMap));
            
            LinkedHashMap<String,Object> message = new LinkedHashMap<>();
            message.put("messageId", contentMap.get("messageId").toString());
            ArrayList<Object> permissions = new ArrayList<>();
            sessionUser.getRole().getPermissions().forEach(permission -> {
                permissions.add(permission.getName());
            });
            message.put("permissions", permissions);
            sendMessage(objectMapper.writeValueAsString(message));
            return true;
        } else if (token == null || token.isEmpty()) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "error");
            dataMap.put("data", "token is empty");
            dataMap.put("messageId", contentMap.get("messageId").toString());
            sendMessage(objectMapper.writeValueAsString(dataMap));
            return true;
        } else if (!jwtTokenProvider.validateToken(token)) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("type", "error");
            dataMap.put("data", "token is invalid");
            dataMap.put("messageId", contentMap.get("messageId").toString());
            sendMessage(objectMapper.writeValueAsString(dataMap));
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
                sendMessage(objectMapper.writeValueAsString(dataMap));
            }
            return !sessionUser.isEnabled();
        } else {
            return true;
        }
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(session.getBasicRemote().toString(),error);
    }
    
    public void sendMessage(LinkedHashMap<String,Object> map) throws IOException {
        sendMessage(objectMapper.writeValueAsString(map));
    }
    
    public void sendMessageWithOutLog(LinkedHashMap<String,Object> map) throws IOException {
        sendMessage(objectMapper.writeValueAsString(map));
    }
    
    public void sendMessageWithOutLog(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        if (message.length() > 65535) message = message.substring(0, 4096) + "...";
        logger.info("webSocket to {}({}):{}", sessionUser.getName(), sid, message);
    }
    
    @NonNull
    @Override
    public List<String> getSubProtocols() {
        return new java.util.ArrayList<>();
    }
    
    public boolean isOpen() {
        return session.isOpen();
    }
}