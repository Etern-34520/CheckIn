package indi.etern.checkIn.service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.api.webSocket.Connector;
import indi.etern.checkIn.entities.user.User;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static indi.etern.checkIn.api.webSocket.Connector.CONNECTORS;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    public static WebSocketService singletonInstance;
    private final ObjectMapper objectMapper;
    
    protected WebSocketService(ObjectMapper objectMapper) {
        singletonInstance = this;
        this.objectMapper = objectMapper;
    }
    
    public void sendMessages(String message, HashSet<String> toSids) {
        logger.info("webSocket to:{}, msg:{}", toSids, message);
        for (Connector item : CONNECTORS) {
            try {
                if (toSids.isEmpty()) {
                    item.sendMessage(message);
                } else if (toSids.contains(item.getSid())) {
                    item.sendMessage(message);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    public void sendMessage(String message, String sid) {
        logger.info("webSocket to:{}, msg:{}", sid, message);
        for (Connector item : CONNECTORS) {
            try {
                if (item.getSid().equals(sid)) {
                    item.sendMessage(message);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    public void sendMessageToAll(String message) {
        logger.info("webSocket to all, msg:{}", message);
        sendMessageToAllWithoutLog(message);
    }
    
    public boolean isOnline(String sid) {
        boolean online = false;
        for (Connector item : CONNECTORS) {
            if (item.getSid().equals(sid)) {
                online = true;
                break;
            }
        }
        return online;
    }
    
    public boolean isOnline(User user) {
        return isOnline(String.valueOf(user.getQQNumber()));
    }
    
    @SneakyThrows
    public void sendMessageToAll(LinkedHashMap<String,Object> map) {
        sendMessageToAll(objectMapper.writeValueAsString(map));
    }

    @SneakyThrows
    public void sendMessageToAllWithoutLog(LinkedHashMap<String,Object> map) {
        sendMessageToAllWithoutLog(objectMapper.writeValueAsString(map));
    }

    public void sendMessageToAllWithoutLog(String message) {
        for (Connector item : CONNECTORS) {
            if (item.isOpen()) {
                try {
                    item.sendMessageWithOutLog(message);
                } catch (IllegalStateException ignored) {
                } catch (Exception e) {
                    logger.error("error occurred when send to sid_{}", item.getSid(), e);
                }
            }
        }
    }
}
