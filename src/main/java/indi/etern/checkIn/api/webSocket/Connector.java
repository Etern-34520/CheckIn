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
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
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
    private final int BUFFER_SIZE = 64 * 1024;
    private final int LOG_TRUNCATE_SIZE = 512;
    private Session session;
    @Getter
    private String sid = "";
    private User sessionUser;
    private final ObjectMapper objectMapper = CheckInApplication.getObjectMapper();
    
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
        session.setMaxTextMessageBufferSize(BUFFER_SIZE);//64 KB
        session.setMaxBinaryMessageBufferSize(0);
        CONNECTORS.add(this);
        this.sid = sid;
        addOnlineCount();
        logger.info("sid_{}:connected", sid);
    }
    
    @SneakyThrows
    @OnClose
    public void onClose() {
        CONNECTORS.remove(this);
        webSocketService.unsubscribeAllChannels(sid);
        subOnlineCount();
        logger.info("sid_{}:close", sid);
    }
    
    private final Map<String, PartRawMessageProcessor> partMessageMap = new HashMap<>();
    
    private record PartMessageData(List<String> messageIds, String partId, String messagePart) {
    }
    
    private record ChannelMessageData(@NonNull String channel) {
    }
    
    @OnMessage
    public void onMessage(String message) {
        final byte[] decoded = Base64.getDecoder().decode(message);
        try {
            JsonRawMessage contextJsonMessage = objectMapper.readValue(decoded, JsonRawMessage.class);
            if (!checkToken(contextJsonMessage)) {
                return;
            }
            String contextId = contextJsonMessage.getMessageId();
            
            final String typeName = contextJsonMessage.getType().getName();
            switch (typeName) {
                case "partMessage" -> {
                    PartMessageData partMessageData = objectMapper.readValue(contextJsonMessage.getData(), PartMessageData.class);
                    PartRawMessageProcessor partRawMessageProcessor;
                    String partId;
                    if (partMessageData.messageIds != null) {
                        partRawMessageProcessor = new PartRawMessageProcessor(partMessageData.messageIds);
                        partMessageMap.put(contextId, partRawMessageProcessor);
                        partId = contextId;
                        sendMessage("{\"type\":\"success\",\"messageId\":\"" + contextId + "\"}");
                    } else if (partMessageData.partId != null && partMessageData.messagePart != null) {
                        partId = partMessageData.partId;
                        partRawMessageProcessor = partMessageMap.get(partId);
                        partRawMessageProcessor.put(contextId, partMessageData.messagePart);
                    } else {
                        sendMessage("{\"type\":\"error\",\"message\":\"not supported message format\",\"messageId\":\"" + contextId + "\"}");
                        return;
                    }
                    if (partRawMessageProcessor.isComplete()) {
                        onMessage(partRawMessageProcessor.toString());
                        partMessageMap.remove(partId);
                    }
                }
                case "subscribe" -> {
                    ChannelMessageData channelMessageData = objectMapper.readValue(contextJsonMessage.getData(), ChannelMessageData.class);
                    final String channelName = channelMessageData.channel;
                    logger.debug("websocket subscribe from {}({}): channel \"{}\"", sessionUser.getName(), sessionUser.getQQNumber(), channelName);
                    webSocketService.subscribeChannel(sid, channelName);
                    sendMessage("{\"type\":\"success\",\"messageId\":\"" + contextId + "\"}");
                }
                case "unsubscribe" -> {
                    ChannelMessageData channelMessageData = objectMapper.readValue(contextJsonMessage.getData(), ChannelMessageData.class);
                    final String channelName = channelMessageData.channel;
                    logger.debug("websocket unsubscribe from {}({}): channel \"{}\"", sessionUser.getName(), sessionUser.getQQNumber(), channelName);
                    webSocketService.unsubscribeChannel(sid, channelName);
                    sendMessage("{\"type\":\"success\",\"messageId\":\"" + contextId + "\"}");
                }
                default -> {
                    String logMessage = new String(decoded);
                    if (logMessage.length() > LOG_TRUNCATE_SIZE) {
                        logMessage = logMessage.substring(0, LOG_TRUNCATE_SIZE) + "\n=========(truncated)=========";
                    }
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
                            JsonRawMessage jsonRawMessage = new JsonRawMessage(type, contextId, optionalJsonResult.get());
                            sendMessage(jsonRawMessage);
                        }
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            e.printStackTrace();
                        }
                        logger.error("{} : {}", e.getClass().getName(), e.getMessage());
                        String errorRawMessage = new String(decoded);
                        logger.debug("Exception caused by message from {}({}):{}", sessionUser.getName(), sid, errorRawMessage);
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
                String messageId = objectMapper.readValue(decoded, HashMap.class).get("messageId").toString();
                sendError(messageId, e.getClass().getSimpleName() + ":" + e.getMessage());
            } catch (IOException exception) {
                String logMessage = new String(decoded);
                logger.error("while sending error message (caused by \"{}\") to {}({}):{}", logMessage, sessionUser.getName(), sid, exception.getMessage());
                exception.printStackTrace();
            }
            e.printStackTrace();
        }
    }
    
    private void sendError(String contextId, String data) throws IOException {
        Message<String> message = Message.error(contextId, data);
        sendMessage(message);
    }
    
    private record TokenMessage(String token) {
    }
    
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
                    new SendPermissionsToUsersAction.Input(List.of(sessionUser)));
            ;
            return false;
        } else if (sessionUser != null) {
            JwtAuthenticationFilter.setUserToSecurityContextHolder(sessionUser);
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
    
    public void sendMessage(IMessage<?> message) throws IOException {
        final String messageString = objectMapper.writeValueAsString(message);
        sendMessage(messageString);
    }
    
    public void sendMessageWithOutLog(String messageString) throws IOException {
        this.session.getBasicRemote().sendText(Base64.getEncoder().encodeToString(messageString.getBytes()));
    }
    
    public void sendMessage(String messageString) throws IOException {
        sendMessageWithOutLog(messageString);
        if (logger.isDebugEnabled()) {
            if (messageString.length() > LOG_TRUNCATE_SIZE)
                messageString = messageString.substring(0, LOG_TRUNCATE_SIZE) + "\n=========(truncated)=========";
            logger.debug("webSocket to {}({}):{}", sessionUser.getName(), sid, messageString);
        }
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