package indi.etern.checkIn.service.web;

import com.google.gson.JsonObject;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.api.webSocket.Connector;
import indi.etern.checkIn.entities.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;

import static indi.etern.checkIn.api.webSocket.Connector.CONNECTORS;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(CheckInApplication.class);
    public static WebSocketService singletonInstance;
    protected WebSocketService() {
        singletonInstance = this;
    }
    /**
     * 群发自定义消息
     */
    public void sendMessages(String message, HashSet<String> toSids) {
        logger.info("webSocket to:{}, msg:{}", toSids, message);
        for (Connector item : CONNECTORS) {
            try {
                //这里可以设定只推送给传入的sid，为null则全部推送
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
    
    public void sendMessageToAll(JsonObject jsonObject) {
        sendMessageToAll(jsonObject.toString());
    }
}
