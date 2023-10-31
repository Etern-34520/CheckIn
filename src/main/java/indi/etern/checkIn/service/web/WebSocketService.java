package indi.etern.checkIn.service.web;

import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.api.webSocket.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;

import static indi.etern.checkIn.api.webSocket.Connector.CONNECTORS;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(CheckInApplication.class);
    
    /**
     * 群发自定义消息
     */
    public void sendMessages(String message, HashSet<String> toSids) throws IOException {
        logger.info("webSocket to:" + toSids + ", msg:" + message);
        
        for (Connector item : CONNECTORS) {
            try {
                //这里可以设定只推送给传入的sid，为null则全部推送
                if (toSids.isEmpty()) {
                    item.sendMessage(message);
                } else if (toSids.contains(item.getSid())) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }
    
    public void sendMessage(String message, String sid) throws IOException {
        logger.info("webSocket to:" + sid + ", msg:" + message);
        
        for (Connector item : CONNECTORS) {
            try {
                if (item.getSid().equals(sid)) {
                    item.sendMessage(message);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    public void sendMessageToAll(String message) throws IOException {
        logger.info("webSocket to all, msg:" + message);
        for (Connector item : CONNECTORS) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                logger.error("send : error",e);
            }
        }
    }
}
