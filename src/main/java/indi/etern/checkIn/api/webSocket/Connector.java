package indi.etern.checkIn.api.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.interfaces.ResultJsonContext;
import indi.etern.checkIn.action.role.SendPermissionsToUsersAction;
import indi.etern.checkIn.api.webSocket.interfaces.IMessage;
import indi.etern.checkIn.auth.JwtAuthenticationFilter;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.web.WebSocketService;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
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
    public static final CopyOnWriteArraySet<Connector> CONNECTORS = new CopyOnWriteArraySet<>();
    public static final Logger logger = LoggerFactory.getLogger(Connector.class);
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static WebSocketService webSocketService;
    private static JwtTokenProvider jwtTokenProvider;
    private static ActionExecutor actionExecutor;
    private Session session;
    @Getter
    private String sid = "";
    private User sessionUser;
    private final ObjectMapper objectMapper = CheckInApplication.getObjectMapper();
    
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
        session.setMaxTextMessageBufferSize(64 * 1024);//64 KB
//        session.setMaxTextMessageBufferSize(64 * 1024 * 1024);//64 MB
        session.setMaxBinaryMessageBufferSize(0);
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
    
    private final Map<String, PartRawMessage> partMessageMap = new HashMap<>();
    
    private record PartMessageData(List<String> messageIds, String partId, String messagePart) {}
    private record ChannelMessageData(@NonNull String channel) {}
    @OnMessage
    public void onMessage(String message) {
        try {
            JsonRawMessage contextJsonMessage = objectMapper.readValue(message,JsonRawMessage.class);
//            Map<String, Object> contentMap = objectMapper.readValue(message, HashMap.class);
            //TODO
//            JwtAuthenticationFilter.setUserToSecurityContextHolder(sessionUser);
            if (!checkToken(contextJsonMessage)) {
                return;//TODO
            }
            String contextId = contextJsonMessage.getMessageId();
            
            final String typeName = contextJsonMessage.getType().getName();
            switch (typeName) {
                case "partMessage" -> {
                    PartMessageData partMessageData = objectMapper.readValue(contextJsonMessage.getData(),PartMessageData.class);
                    PartRawMessage partRawMessage;
                    String partId;
                    if (partMessageData.messageIds != null) {
                        partRawMessage = new PartRawMessage(partMessageData.messageIds);
                        partMessageMap.put(contextId, partRawMessage);
                        partId = contextId;
                        sendMessage("{\"type\":\"success\",\"messageId\":\"" + contextId + "\"}");
                    } else if (partMessageData.partId != null && partMessageData.messagePart != null) {
                        partId = partMessageData.partId;
                        partRawMessage = partMessageMap.get(partId);
                        partRawMessage.put(contextId, partMessageData.messagePart);
                    } else {
                        sendMessage("{\"type\":\"error\",\"message\":\"not supported message format\",\"messageId\":\"" + contextId + "\"}");
                        return;
                    }
                    if (partRawMessage.isComplete()) {
                        onMessage(partRawMessage.toString());
                        partMessageMap.remove(partId);
                    }
                }
                case "subscribe" -> {
                    ChannelMessageData channelMessageData = objectMapper.readValue(contextJsonMessage.getData(),ChannelMessageData.class);
                    final String channelName = channelMessageData.channel;
                    logger.debug("websocket subscribe from {}({}): channel \"{}\"", sessionUser.getName(),sessionUser.getQQNumber(),channelName);
                    webSocketService.subscribeChannel(sid, channelName);
                    sendMessage("{\"type\":\"success\",\"messageId:\":\"" + contextId + "\"}");
                }
                case "unsubscribe" -> {
                    ChannelMessageData channelMessageData = objectMapper.readValue(contextJsonMessage.getData(),ChannelMessageData.class);
                    final String channelName = channelMessageData.channel;
                    logger.debug("websocket unsubscribe from {}({}): channel \"{}\"", sessionUser.getName(),sessionUser.getQQNumber(),channelName);
                    webSocketService.unsubscribeChannel(sid, channelName);
                    sendMessage("{\"type\":\"success\",\"messageId:\":\"" + contextId + "\"}");
                }
                default -> {
                    String logMessage = message.length() > 4096 ? message.substring(0, 4096) : message;
                    try {
                        ResultJsonContext<OutputData> context = actionExecutor.executeWithJson(typeName, contextJsonMessage.data);
                        logger.debug("{}({}):{}", sessionUser.getName(), sid, logMessage);
                        final Optional<String> optionalJsonResult = context.getOptionalJsonResult();
                        if (optionalJsonResult.isPresent()) {
                            final OutputData output = context.getOutput();
                            IMessage.Type type = switch (output.result()) {
                                case SUCCESS -> IMessage.Type.SUCCESS;
                                case ERROR -> IMessage.Type.ERROR;
                                case WARNING -> IMessage.Type.WARNING;
                            };
                            JsonRawMessage jsonRawMessage = new JsonRawMessage(type,contextId,optionalJsonResult.get());
                            sendMessage(jsonRawMessage);//FIXME force closed when the message is too long
                        }
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            e.printStackTrace();
                        }
                        logger.error("{} : {}", e.getClass().getName(), e.getMessage());
                        logger.debug("Exception caused by message from {}({}):{}", sessionUser.getName(), sid, logMessage);
                        if (e instanceof PermissionDeniedException permissionDeniedException) {
                            sendError(contextId, permissionDeniedException.getMessage());
                        } else
                            sendError(contextId, e.getMessage());
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
    
    private void sendError(String contextId, String data) throws IOException {
        Message<String> message = Message.error(contextId, data);
        sendMessage(message);
    }
    
    private record TokenMessage(String token){}
    
    private boolean checkToken(JsonRawMessage message) throws IOException {
        if (message.getType().equals(Message.Type.of("token"))) {
            final TokenMessage tokenMessage = objectMapper.readValue(message.getData(), TokenMessage.class);
            sessionUser = jwtTokenProvider.getUser(tokenMessage.token);
            JwtAuthenticationFilter.setUserToSecurityContextHolder(sessionUser);
            if (!sid.equals(String.valueOf(sessionUser.getQQNumber()))) {
                sendError(message.getMessageId(), "sid is not equal to qq");
                return false;
            }
            
            actionExecutor.execute(SendPermissionsToUsersAction.class,
                    new SendPermissionsToUsersAction.Input(List.of(sessionUser)));;
            return false;
        } else if (sessionUser != null) {
            JwtAuthenticationFilter.setUserToSecurityContextHolder(sessionUser);
/*
            if (sessionUser.isEnabled()) {
*/
/*
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        sessionUser,
                        null,
                        sessionUser.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
*//*

            }
*/
            if (!sessionUser.isEnabled()) {
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("type", "error");
                dataMap.put("data", "user is disabled");
                dataMap.put("messageId", message.getMessageId());
                sendMessage(objectMapper.writeValueAsString(dataMap));
            }
            return sessionUser.isEnabled();
        } else {
            return false;
        }
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(session.getBasicRemote().toString(),error);
    }
    
    public void sendMessage(LinkedHashMap<String,Object> map) throws IOException {
        sendMessage(objectMapper.writeValueAsString(map));
    }
    
    public void sendMessage(IMessage<?> message) throws IOException {
        sendMessage(objectMapper.writeValueAsString(message));
    }
    
    public void sendMessageWithOutLog(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        if (message.length() > 8192) message = message.substring(0, 8192) + "\n=========(truncated)=========";
        logger.debug("webSocket to {}({}):{}", sessionUser.getName(), sid, message);
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